import { mount } from '@vue/test-utils'
import HomeView from './HomeView.vue'

describe('HomeView', () => {
  it('renders the public homepage content', () => {
    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' }
        }
      }
    })

    expect(wrapper.text()).toContain('AI 知识库问答与工单协同处理平台')
    expect(wrapper.text()).toContain('先回答，再流转')
    expect(wrapper.text()).toContain('知识库可追溯')
    expect(wrapper.text()).toContain('工单闭环')
    expect(wrapper.text()).toContain('管理端看得见')
    expect(wrapper.find('[data-component="herosolutions"]').exists()).toBe(true)
    expect(wrapper.find('[data-component="gallerysticky"]').exists()).toBe(true)
    expect(wrapper.find('[data-component="tablethreecols"]').exists()).toBe(true)
    expect(wrapper.find('[data-component="dropdownssticky"]').exists()).toBe(true)
    expect(wrapper.findAll('.component--gallerysticky .image img')).toHaveLength(4)
    expect(wrapper.text()).toContain('用户场景')
  })
})
