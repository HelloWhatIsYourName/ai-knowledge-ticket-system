# Public Homepage and Product Shell Design

## Design Read

Reading this as: a B2B SaaS public homepage for thesis review, demonstration, and future users, with a credible white premium language, leaning toward a Tresmares-inspired narrative homepage plus a Claude/Zendesk-style product workspace.

## Goal

Build a public first impression that makes the AI knowledge ticket system feel credible, finished, and worth using, then hand users into a quiet operational app where chat, tickets, knowledge, and admin workflows can grow without redesigning the shell.

## Direction

The public homepage should use a white premium base, generous spacing, strong typography, and restrained scroll storytelling. It can borrow the structure of the prior reference page: large first viewport, compact proof metrics, sticky visual narrative, capability tables, and related workflow sections.

The logged-in product area should be calmer and more utilitarian. It should use Zendesk-like service-console structure and Claude-like whitespace: sidebar navigation, clear page titles, low-noise panels, and predictable forms/tables.

Apple-style liquid glass is only a local material accent. It should appear on the login panel, floating AI citation surfaces, or a small homepage callout. It should not dominate dashboards, tables, ticket lists, or admin forms.

## Visual System

- Base theme: light mode first, using off-white and cool neutral surfaces.
- Accent: one restrained blue-green or indigo trust color across the whole frontend.
- Typography: modern sans-serif, preferably Geist-like if available through local package or self-hosted assets. Inter is acceptable only if dependency or delivery constraints make it the pragmatic choice.
- Radius: one consistent radius scale. Product controls can use 8px, homepage feature panels can use 12px to 16px, and primary CTAs can be pill-shaped if used consistently.
- Cards: only for repeated items, product panels, and dashboard widgets. Page sections should use full-width bands or open layouts.
- Images: homepage needs real visual assets or generated product-oriented imagery. Do not reuse Tresmares images or saved third-party assets.

## Homepage Information Architecture

### Routes

- `/` is the public homepage.
- `/login` is the authentication entry.
- `/app` is the logged-in product shell.

### Navigation

Desktop navigation should fit in one line with a maximum height of 72px:

- Product
- Workflow
- Trust
- Login

Mobile navigation should collapse to a simple menu without duplicating content.

### Hero

Hero copy should be short and credible:

- Headline: `AI 知识库问答与工单协同处理平台`
- Supporting copy: `先用知识库回答问题，再把复杂问题流转成可追踪工单。`
- Primary CTA: `进入系统`
- Secondary CTA: `查看流程`

The hero visual should show the system concept, not a fake decorative dashboard. Acceptable options:

- A real mini component preview wired from homepage data structures.
- A generated product image that depicts question, retrieval, answer, ticket, and statistics flow.
- A simple animated flow diagram using DOM elements and icon-library icons.

### Proof Metrics

The metric strip should show product capabilities, not fake business claims:

- RAG 问答
- 向量检索
- 工单闭环
- RBAC 权限
- 管理统计

Numbers should only appear if they come from real backend/demo data or are clearly marked as sample values in code.

### Narrative Sections

The homepage should include four narrative sections:

- `先回答，再流转`: 用户问题先进入知识库检索，命中后直接回答。
- `知识库可追溯`: AI 回答展示引用来源和相似度证据。
- `工单闭环`: 未解决问题进入工单状态机，支持回复和内部备注。
- `管理端看得见`: 管理员查看分类、热门问题、处理效率和知识命中情况。

### Capability Table

Replace the reference site's financial table with a product capability comparison:

| Capability | User Value | System Contract |
| --- | --- | --- |
| AI 问答 | 快速获得可追溯答案 | RAG chat API, citation list |
| 工单流转 | 复杂问题进入处理队列 | ticket, ticket_comment |
| 知识管理 | 维护材料并生成向量 | Oracle Vector, embedding provider |
| 权限管理 | 管理员和普通用户隔离 | RBAC menus and permissions |
| 统计分析 | 看见服务质量和热点 | admin statistics APIs |

### Footer

Footer should be compact:

- Product name
- Thesis/demo context
- GitHub repository link
- Login link

No decorative version stamps.

## Product Shell Design

The app shell should support growth across later pages:

- Sidebar navigation from backend `menus`.
- Top bar with user summary and logout.
- Route content area with predictable page header.
- Empty, loading, and error states as reusable patterns.
- Admin dashboard as the first real feature page.

The shell should stay visually quiet. It should not inherit homepage scroll animations.

## Animation Standard

The homepage should aim for the old reference page's smoothness through restraint and verification, not through heavy effects.

Allowed animation:

- Hero text and visual enter with opacity and translate.
- Sticky narrative section for the product flow.
- Small hover feedback on CTAs and navigation.
- Subtle metric reveal when entering viewport.

Disallowed animation:

- Custom mouse cursor.
- Per-frame Vue state updates from scroll.
- Animation of layout properties such as width, height, top, or left.
- Multiple marquees.
- Heavy WebGL for the first implementation slice.

Implementation rules:

- Animate only `transform` and `opacity`.
- Use GSAP ScrollTrigger or IntersectionObserver for scroll-linked narrative.
- Use CSS transitions for simple hover and entry effects.
- Clean up all scroll triggers when components unmount.
- Respect `prefers-reduced-motion`.
- Keep homepage animation isolated from product shell components.

Verification targets:

- No visible hero layout shift.
- Desktop and mobile screenshots show no overlapping text.
- Reduced-motion mode produces a usable static page.
- Lighthouse should not show obvious CLS or long main-thread blocking caused by animation.

## Accessibility

- All CTAs must pass contrast.
- Keyboard focus states must be visible.
- Login inputs use labels above fields, never placeholder-only labels.
- Decorative visuals use empty alt text, meaningful visuals use descriptive alt text.
- Motion-heavy sections must remain understandable when motion is reduced.

## Content Voice

Use plain product language. Avoid poetic or vague phrasing.

Preferred:

- `先回答，再流转`
- `答案带引用，处理有记录`
- `从用户问题到管理统计，一条链路闭环`

Avoid:

- Generic AI-purple marketing claims.
- Fake-perfect numbers.
- Startup filler words such as "next-gen" or "unleash".

## Implementation Scope For Phase 8

Phase 8 should build:

- Vue3 frontend scaffold.
- Public homepage route.
- Login route and auth store.
- RBAC app shell.
- Admin statistics dashboard.
- Verification report with screenshots or browser notes.

Phase 8 should not build:

- Full RAG chat page.
- Full ticket list/detail UI.
- Full knowledge management UI.
- Full user/role CRUD forms.

Those should become later frontend phases after the shell is stable.
