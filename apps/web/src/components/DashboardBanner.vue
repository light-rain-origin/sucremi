<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="dashboard-banner" aria-labelledby="banner-title">
    <div class="banner-glow" aria-hidden="true" />
    <span class="banner-sparkle sparkle-one" aria-hidden="true" />
    <span class="banner-sparkle sparkle-two" aria-hidden="true" />
    <span class="banner-sparkle sparkle-three" aria-hidden="true" />
    <span class="banner-orb orb-one" aria-hidden="true" />
    <span class="banner-orb orb-two" aria-hidden="true" />

    <div class="banner-content">
      <h2 id="banner-title">早上好，今天也慢慢来！</h2>
      <p>欢迎回到你的专属开发空间。<br />先找回上次的进度，再挑一个小目标继续前进吧。</p>
      <div class="banner-actions">
        <RouterLink class="banner-button primary" :to="{ path: '/projects', query: { create: '1' } }">
          <span class="plus" aria-hidden="true">+</span>
          新建项目
        </RouterLink>
        <RouterLink class="banner-button secondary" to="/projects">查看所有项目</RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.dashboard-banner {
  position: relative;
  display: flex;
  min-height: 220px;
  align-items: center;
  overflow: hidden;
  margin: 14px 0 0;
  padding: 26px 64px;
  border: 1px solid var(--sc-banner-border);
  border-radius: 30px;
  background: var(--sc-banner-surface);
  color: var(--sc-banner-text);
  isolation: isolate;
}

.banner-glow {
  position: absolute;
  z-index: -1;
  top: 10%;
  right: 7%;
  width: 64%;
  height: 80%;
  border-radius: 50%;
  background: var(--sc-banner-glow);
  filter: blur(26px);
}

.banner-content {
  position: relative;
  z-index: 1;
  max-width: 610px;
}

.banner-content h2 {
  margin: 0 0 9px;
  color: var(--sc-banner-text);
  font-size: 28px;
  font-weight: 650;
  letter-spacing: 0.015em;
  line-height: 1.35;
}

.banner-content p {
  margin: 0;
  color: var(--sc-banner-description);
  font-size: 14px;
  line-height: 1.8;
}

.banner-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 27px;
}

.banner-button {
  display: inline-flex;
  min-width: 146px;
  height: 48px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 20px;
  border: 1px solid transparent;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease;
}

.banner-button:hover {
  transform: translateY(-1px);
}

.banner-button.primary {
  background: var(--sc-banner-primary);
  box-shadow: var(--sc-banner-primary-shadow);
  color: #fff;
}

.banner-button.primary:hover {
  box-shadow: var(--sc-banner-primary-shadow-hover);
}

.banner-button.secondary {
  border-color: var(--sc-banner-secondary-border);
  background: var(--sc-banner-secondary);
  color: var(--sc-banner-text);
}

.banner-button.secondary:hover {
  background: var(--sc-banner-secondary-hover);
  box-shadow: var(--sc-banner-secondary-hover-shadow);
}

.plus {
  font-size: 21px;
  font-weight: 400;
  line-height: 1;
}

.banner-sparkle {
  position: absolute;
  z-index: 0;
  width: 21px;
  aspect-ratio: 1;
  background: var(--sc-banner-sparkle);
  clip-path: polygon(50% 0, 62% 38%, 100% 50%, 62% 62%, 50% 100%, 38% 62%, 0 50%, 38% 38%);
}

.sparkle-one {
  top: 34px;
  left: 30px;
}

.sparkle-two {
  top: 34px;
  right: 30%;
  width: 27px;
  opacity: 0.55;
}

.sparkle-three {
  right: 8%;
  bottom: 28px;
  width: 17px;
  background: var(--sc-banner-sparkle-alt);
  opacity: 0.7;
}

.banner-orb {
  position: absolute;
  z-index: -1;
  border: 1px solid var(--sc-banner-orb-border);
  border-radius: 50%;
  background: var(--sc-banner-orb-surface);
}

.orb-one {
  top: -44px;
  right: 4%;
  width: 166px;
  height: 166px;
}

.orb-two {
  right: 16%;
  bottom: -106px;
  width: 210px;
  height: 210px;
}

@media (max-width: 768px) {
  .dashboard-banner {
    min-height: 220px;
    padding: 26px 24px;
    border-radius: 22px;
  }

  .banner-content h2 {
    font-size: 23px;
  }

  .banner-content p {
    max-width: 390px;
    font-size: 13px;
  }

  .banner-actions {
    gap: 10px;
    margin-top: 20px;
  }

  .banner-button {
    min-width: 0;
    height: 44px;
    padding: 0 16px;
    border-radius: 14px;
  }

  .sparkle-one {
    top: 20px;
    left: auto;
    right: 25px;
  }

  .sparkle-two,
  .orb-two {
    display: none;
  }
}
</style>
