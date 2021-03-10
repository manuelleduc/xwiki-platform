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
import DisplayerBoolean from "../../../displayers/DisplayerBoolean";


function initWrapper(props, logic) {
  return mount(DisplayerBoolean, {
    propsData: Object.assign({
      viewOnly: false,
      isView: true,
      propertyId: 'propertyIdTest',
      entry: {propertyIdTest: true}
    }, props),
    provide: {
      logic: Object.assign({
        isEditable() {
          return true;
        }
      }, logic)
    }
  });
}

describe('DisplayerBoolean.vue', () => {
  it('Renders a true entry in view mode', () => {
    const wrapper = initWrapper();
    expect(wrapper.text()).toMatch('True')
  })

  it('Renders a false entry in view mode', () => {
    const wrapper = initWrapper({entry: {propertyIdTest: false}});
    expect(wrapper.text()).toMatch('False')
  })

  it('Renders a true entry in edit mode', async () => {
    const wrapper = initWrapper();

    const viewerDiv = wrapper.find('div[tabindex="0"]');
    await viewerDiv.trigger('dblclick')

    const checkbox = wrapper.find('input');

    expect(checkbox.element.checked).toBe(true)
  })

  it('Renders a false entry in edit mode', async () => {
    const wrapper = initWrapper({entry: {propertyIdTest: false}});

    const viewerDiv = wrapper.find('div[tabindex="0"]');
    await viewerDiv.trigger('dblclick')

    const checkbox = wrapper.find('input');

    expect(checkbox.element.checked).toBe(false);
  })

  it('Send events after the end of the edit', async () => {
    var values = []
    const wrapper = initWrapper({}, {
      setValue(value) {
        values.push(value)
      }
    });

    const viewerDiv = wrapper.find('div[tabindex="0"]');
    await viewerDiv.trigger('dblclick')

    const checkbox = wrapper.find('input');

    await checkbox.setChecked(false);

    await wrapper.find('div[tabindex="0"]').trigger('keypress.enter');

    expect([...values]).toMatchObject([{
      entry: {
        propertyIdTest: true,
      },
      propertyId: "propertyIdTest",
      value: false
    }]);
  })


});