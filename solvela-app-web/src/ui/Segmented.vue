<script setup lang="ts" generic="T extends string">
/**
 * 两三个选项之间二选一的分段控件。
 *
 * <h3>为什么不是下拉框</h3>
 * 选项只有两三个、而且是「这一页要填什么」这种决定时，下拉框把选项藏了起来 ——
 * 用户得先点开才知道原来还能用邮箱注册。分段控件把所有出路摆在明面上，
 * 这正是它在登录/注册页要做的事。
 *
 * <h3>用 button 而不是 div + @click</h3>
 * 后者键盘按不到，读屏也不会说这是个可以按的东西。
 * {@code type="button"} 必须写死：这个控件几乎总是放在 form 里，
 * 不写的话点它会触发表单提交。
 */
const model = defineModel<T>({ required: true })

defineProps<{ options: readonly { value: T; label: string }[] }>()
</script>

<template>
  <div class="sv-segmented" role="tablist">
    <button
      v-for="option in options"
      :key="option.value"
      class="sv-segmented__item"
      :class="{ 'sv-segmented__item--active': model === option.value }"
      type="button"
      role="tab"
      :aria-selected="model === option.value"
      @click="model = option.value"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<style scoped>
.sv-segmented {
  display: flex;
  gap: 2px;
  padding: 3px;
  border-radius: var(--sv-radius-pill);
  background: var(--sv-bg-fill);
}

.sv-segmented__item {
  flex: 1;
  min-width: 0;
  height: 34px;
  border: 0;
  border-radius: var(--sv-radius-pill);
  background: transparent;
  color: var(--sv-text-secondary);
  font: inherit;
  font-size: var(--sv-font-caption);
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
}

/*
 * 选中态用白底 + 深色字，而不是主色底。
 * 主色底会让这个控件比它下面的主按钮还抢眼 —— 而它只是「选个填法」，
 * 真正的主操作是底下那个「注册」。
 */
.sv-segmented__item--active {
  background: var(--sv-bg-surface);
  color: var(--sv-text-primary);
  font-weight: 500;
}

.sv-segmented__item:focus-visible {
  outline: 2px solid var(--sv-color-primary);
  outline-offset: 1px;
}
</style>
