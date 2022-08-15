import {shallowMount} from '@vue/test-utils';
import App from '@/App.vue';

describe('App.vue', () => {
  it('renders', () => {
    const wrapper = shallowMount(App);
    // TODO: improve tests to validate something relevant.
    expect(wrapper.find('label').text()).toMatch('Hidden Macro Categories');
  });
});
