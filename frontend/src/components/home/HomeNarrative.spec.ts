import { mount } from '@vue/test-utils'
import HomeNarrative from './HomeNarrative.vue'

describe('HomeNarrative', () => {
  it('keeps narrative panels available without animation', () => {
    const wrapper = mount(HomeNarrative)

    expect(wrapper.text()).toContain('用户问题')
    expect(wrapper.text()).toContain('知识检索')
    expect(wrapper.text()).toContain('AI 回答')
    expect(wrapper.text()).toContain('转入工单')
  })
})
