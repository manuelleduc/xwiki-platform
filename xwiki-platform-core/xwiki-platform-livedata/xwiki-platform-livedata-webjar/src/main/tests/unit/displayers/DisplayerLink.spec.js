/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import {mount} from '@vue/test-utils'
import DisplayerLink from "../../../displayers/DisplayerLink";

function initWrapper(options) {
  options = options || {}
  return mount(DisplayerLink, {
      propsData: Object.assign({
        viewOnly: false,
        isView: true,
        propertyId: 'propertyIdTest',
        entry: {
          propertyIdTest: 'entryA1',
          propertyIdTestHref: 'entryLink'
        }
      }, options.propsData),
      provide: {
        logic: Object.assign({
          isEditable() {
            return true;
          },
          getDisplayerDescriptor() {
            return {
              propertyHref: 'propertyIdTestHref'
            };
          },
          isActionAllowed(action) {
            return action === 'action1';
          },
          getActionDescriptor() {
            return undefined;
          }
        }, (options.provide || {}).logic)
      }
    }
  );
}

describe('DisplayerLink.vue', () => {

  it('Renders an entry in view mode', () => {
    const wrapper = initWrapper();
    expect(wrapper.text()).toMatch('entryA1')
    expect(wrapper.find('a').element.href).toBe('http://localhost/entryLink');
  })

  it('Renders an entry in edit mode', async () => {
    const wrapper = initWrapper();
    const viewerDiv = wrapper.find('div[tabindex="0"]');
    await viewerDiv.trigger('dblclick');
    expect(wrapper.find('input').element.value).toBe("entryA1")
  })

  it('Send events after the end of the edit', async () => {
    const values = [];
    const wrapper = initWrapper({
      provide: {
        logic: {
          setValue(value) {
            values.push(value);
          },
          getDisplayerDescriptor() {
            return {
              html: false
            };
          }
        }
      }
    });
    const viewerDiv = wrapper.find('div[tabindex="0"]');
    await viewerDiv.trigger('dblclick');

    let inputField = wrapper.find('input');

    await inputField.setValue('New Value');
    await wrapper.find('div[tabindex="0"]').trigger('keypress.enter');

    expect([...values]).toMatchObject([{
      entry: {
        propertyIdTest: "entryA1",
      },
      propertyId: "propertyIdTest",
      value: "New Value"
    }]);
  })
})