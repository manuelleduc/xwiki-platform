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
import {shallowMount} from '@vue/test-utils';
import LayoutCards from '../../../../layouts/cards/LayoutCards';
import _ from 'lodash';
import LayoutCardsCard from "../../../../layouts/cards/LayoutCardsCard";

/**
 * TODO: document
 * @param options
 * @returns {*}
 */
function initWrapper(options) {
  return shallowMount(LayoutCards, _.merge({
    provide: {
      logic: {
        canAddEntry: () => false,
        getEntryId: (e) => e.id,
        data: {
          data: {
            entries: []
          }
        }
      }
    },
    mocks: {
      $t: (key) => key
    }
  }, options))
}

describe('LayoutCards.vue', () => {
  it('Renders when no entries', () => {
    const wrapper = initWrapper();
    expect(wrapper.find('.noentries-card').text()).toBe('livedata.bottombar.noEntries');
  })

  it('Renders with some entries', () => {
    const wrapper = initWrapper({
      provide: {
        logic: {
          data: {
            data: {
              entries: [
                {id: 1}
              ]
            }
          }
        }
      }
    });
    let cards = wrapper.findAllComponents(LayoutCardsCard);
    expect(cards.length).toBe(1);
    expect(cards.at(0).props()).toStrictEqual({entry: {id: 1}});
    expect(wrapper.find('.noentries-card').exists()).toBe(false);
  })

})