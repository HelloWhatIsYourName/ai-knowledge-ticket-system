<template>
  <section class="home-hero" aria-labelledby="home-hero-title">
    <header class="home-nav" aria-label="Public navigation">
      <RouterLink class="home-brand" to="/">AI Knowledge Ticket</RouterLink>
      <nav class="home-nav-links">
        <a href="#product">Product</a>
        <a href="#workflow">Workflow</a>
        <a href="#trust">Trust</a>
      </nav>
      <RouterLink class="home-nav-login" to="/login">Login</RouterLink>
    </header>

    <div class="home-hero-grid">
      <div class="home-hero-copy">
        <p class="home-kicker">Knowledge service system</p>
        <h2 id="home-hero-title">AI 知识库问答与工单协同处理平台</h2>
        <p class="home-hero-subtitle">先用知识库回答问题，再把复杂问题流转成可追踪工单。</p>
        <div class="home-hero-actions" aria-label="Homepage actions">
          <RouterLink class="home-button home-button-primary" to="/login">进入系统</RouterLink>
          <a class="home-button home-button-secondary" href="#workflow">查看流程</a>
        </div>
      </div>

      <div class="home-flow-preview" aria-label="Product workflow preview">
        <div class="flow-column">
          <span class="flow-label">用户问题</span>
          <strong>账号登录后如何重置知识库权限？</strong>
        </div>
        <div class="flow-path" aria-hidden="true"></div>
        <div class="flow-column is-accent">
          <span class="flow-label">知识检索</span>
          <strong>匹配政策、操作手册和历史回复</strong>
        </div>
        <div class="flow-path" aria-hidden="true"></div>
        <div class="flow-column">
          <span class="flow-label">AI 回答</span>
          <strong>答案带引用，处理有记录</strong>
        </div>
        <div class="flow-path" aria-hidden="true"></div>
        <div class="flow-column">
          <span class="flow-label">转入工单</span>
          <strong>复杂问题进入人工处理队列</strong>
        </div>
      </div>
    </div>
  </section>
</template>
