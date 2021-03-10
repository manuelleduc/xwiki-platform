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
import displayerMixin from "../../../displayers/displayerMixin";

const TestComponent = {
  render() {
  },
  title: 'test component',
  mixins: [displayerMixin],
}

function initWrapper() {
  return mount(TestComponent, {
    propsData: {
      entry: {
        testProperty: 'testValue'
      },
      propertyId: 'testProperty'
    },
    provide: {
      logic: {
        getPropertyDescriptor(propertyId) {
          return `returnPropertyDescriptor ${propertyId}`
        },
        getDisplayerDescriptor(propertyId) {
          return `returnConfig ${propertyId}`
        },
        data: 'dataTest'
      }
    }
  });
}

describe("displayerMixin.js", () => {
  describe('computed', function () {
    it('value()', () => {
      const wrapper = initWrapper();
      expect(wrapper.vm.value).toBe('testValue')
    })
    it('propertyDescriptor()', () => {
      const wrapper = initWrapper();
      expect(wrapper.vm.propertyDescriptor).toBe('returnPropertyDescriptor testProperty')
    })
    it('config()', () => {
      const wrapper = initWrapper();
      expect(wrapper.vm.config).toBe('returnConfig testProperty')
    })
    it('data()', () => {
      const wrapper = initWrapper();
      expect(wrapper.vm.data).toBe('dataTest')
    })
  });
})