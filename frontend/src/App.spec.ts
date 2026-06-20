import { mount } from '@vue/test-utils'
import App from './App.vue'

describe('App', () => {
  it('renders the application product name', () => {
    const wrapper = mount(App, {
      global: {
        stubs: ['router-view']
      }
    })

    expect(wrapper.text()).toContain('AI 知识库问答与工单协同处理平台')
  })
})
