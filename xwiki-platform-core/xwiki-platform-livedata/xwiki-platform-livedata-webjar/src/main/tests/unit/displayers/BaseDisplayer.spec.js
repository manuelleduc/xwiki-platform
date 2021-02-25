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
import BaseDisplayer from "../../../displayers/BaseDisplayer";

function initWrapper(mockLogic, props) {
  const wrapper = mount(BaseDisplayer, {
    propsData: Object.assign({
      viewOnly: false,
      isView: true,
      propertyId: 'propertyIdTest',
      entry: {
        propertyIdTest: 'entryA1',
        propertyIdTest2: 'entryB1'
      }
    }, props || {}),
    provide: {logic: mockLogic}
  })
  return wrapper;
}

describe('BaseDisplayer.vue', () => {
  it('Renders an entry in view mode', () => {
    const mockLogic = {
      isEditable() {
        return true;
      }
    };
    const wrapper = initWrapper(mockLogic);

    expect(wrapper.text()).toMatch('entryA1')
  })

  it('Send event on double click', async () => {
    const mockLogic = {
      isEditable() {
        return true;
      }
    };

    const wrapper = initWrapper(mockLogic);

    await wrapper.trigger('dblclick');

    expect(wrapper.emitted()).toEqual({"update:isView": [[false]]})
  })

  it('Renders an entry in edit mode', () => {
    const mockLogic = {
      isEditable() {
        return true;
      }
    };

    const wrapper = initWrapper(mockLogic, {
      isView: false
    });

    expect(wrapper.find('input').element.value).toMatch('entryA1')
  })

})