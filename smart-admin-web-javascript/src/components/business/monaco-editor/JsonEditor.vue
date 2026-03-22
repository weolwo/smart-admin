<template>
  <div class="editor-container" :style="{ height: containerHeight }">
    <div class="ios-toolbar">
      <div class="toolbar-left">
        <button @click="saveToLocal" class="ios-btn ios-btn-secondary" :disabled="isReadOnly"><span class="icon">💾</span> 暂存本地</button>
        <button @click="handlePreSubmit" class="ios-btn ios-btn-primary" :disabled="isReadOnly"><span class="icon">🚀</span> 提交规则</button>
        <div class="divider"></div>
        <button @click="formatCode" class="ios-btn ios-btn-icon" title="一键格式化">✨ 格式化</button>
      </div>

      <div class="toolbar-right">
        <select v-model="currentLanguage" class="ios-select" title="切换语言">
          <option v-for="lang in languageList" :key="lang" :value="lang">
            {{ lang.toUpperCase() }}
          </option>
        </select>
        <select v-model="currentTheme" class="ios-select" title="切换主题">
          <option value="vs-dark">🌙 Dark</option>
          <option value="vs">☀️ Light</option>
        </select>
        <select v-model="containerHeight" class="ios-select" title="窗口大小">
          <option value="300px">🪟 小</option>
          <option value="500px">💻 中</option>
          <option value="800px">🖥️ 大</option>
        </select>
        <button @click="toggleReadOnly" :class="['ios-btn', isReadOnly ? 'ios-btn-danger' : 'ios-btn-warning']">
          {{ isReadOnly ? '🔒 只读' : '✏️ 编辑' }}
        </button>
      </div>
    </div>

    <vue-monaco-editor
      v-model:value="codeContent"
      :theme="currentTheme"
      :language="currentLanguage"
      :options="dynamicOptions"
      @mount="handleMount"
      class="monaco-box"
    />

    <div v-if="showDiffModal" class="ios-modal-overlay">
      <div class="ios-modal-content">
        <div class="modal-header">
          <h3>🧐 提交变更确认 (Diff)</h3>
        </div>
        <div class="diff-editor-wrapper">
          <vue-monaco-diff-editor
            :original="originalCode"
            :modified="codeContent"
            :language="currentLanguage"
            :theme="currentTheme"
            :options="diffOptions"
            class="monaco-box"
          />
        </div>
        <div class="modal-footer">
          <button @click="showDiffModal = false" class="ios-btn ios-btn-secondary">取消</button>
          <button @click="confirmSubmit" class="ios-btn ios-btn-primary">✅ 确认发布！</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, shallowRef, computed, watch, onMounted } from 'vue';
  import { VueMonacoEditor, VueMonacoDiffEditor } from '@guolao/vue-monaco-editor';

  // ==========================================
  // 🌟 0. 模拟后端吐出的 QLExpress 业务字典树
  // ==========================================
  // 想象这是你的 Java 后端通过反射扫描 @QLAlias 和 @ScriptFunction 吐给前端的 JSON
  const qlDictionary = {
    // 1. 实体对象域
    objects: {
      用户: {
        desc: '当前登录的用户信息',
        properties: {
          是vip: { type: 'boolean', desc: '判断该用户是否购买了VIP' },
          用户名: { type: 'string', desc: '用户的真实姓名' },
          积分: { type: 'number', desc: '用户账户剩余积分' },
        },
      },
      订单: {
        desc: '当前处理的交易订单',
        properties: {
          金额: { type: 'number', desc: '订单实付总金额' },
          订单号: { type: 'string', desc: '唯一的业务单号' },
        },
      },
    },
    // 2. 自定义函数域 (带参数插值模板)
    functions: [
      { label: '计算运费', insertText: '计算运费(${1:距离})', desc: '根据距离(公里)计算配送费' },
      { label: '发放优惠券', insertText: '发放优惠券(${1:用户}, ${2:金额})', desc: '核心发券接口' },
    ],
    // 3. 关键字与操作符别名
    keywords: ['如果', '则', '否则', '返回', '大于', '等于', '并且', '或者'],
  };

  // ==========================================
  // 🌟 1. 状态定义
  // ==========================================
  const originalCode = ref('如果 (用户.是vip 并且 订单.金额 大于 100) 则 {\n  返回 计算运费(0);\n} 否则 {\n  返回 计算运费(10);\n}');
  const codeContent = ref('');
  const currentLanguage = ref('qlexpress'); // 🌟 默认换成咱们独创的专属语言！
  const currentTheme = ref('vs-dark');
  const isReadOnly = ref(false);
  const containerHeight = ref('500px');
  const showDiffModal = ref(false);

  // 加入了咱们自己的 qlexpress
  const languageList = ['qlexpress', 'json', 'javascript', 'sql', 'java'];
  const CACHE_KEY = 'ql_editor_draft';

  // ==========================================
  // 🧠 2. 核心神技：注册 QLExpress 语言与智能提示！
  // ==========================================
  let isProviderRegistered = false; // 防抖，防止重复注册内存泄漏

  const handleMount = (editor, monaco) => {
    editorRef.value = editor;

    // 确保这套核武器只初始化一次
    if (!isProviderRegistered) {
      // ⚔️ 步骤 A：向微软 Monaco 宣告一门新语言的诞生！
      monaco.languages.register({ id: 'qlexpress' });

      // 🎨 步骤 B：注册高亮规则 (Lexer)
      monaco.languages.setMonarchTokensProvider('qlexpress', {
        tokenizer: {
          root: [
            // 关键字高亮成紫色
            [/如果|则|否则|返回|大于|等于|并且|或者/, 'keyword'],
            // 对象名高亮成青色
            [/用户|订单/, 'type'],
            // 函数名高亮成黄色
            [/计算运费|发放优惠券/, 'function'],
            // 字符串与数字
            [/"[^"]*"/, 'string'],
            [/[0-9]+/, 'number'],
          ],
        },
      });

      // 🚀 步骤 C：注册极其硬核的智能代码补全 (Language Server Protocol 降级版)
      monaco.languages.registerCompletionItemProvider('qlexpress', {
        triggerCharacters: ['.'], // 当敲下 "." 的时候，触发属性提示

        provideCompletionItems: function (model, position) {
          // 获取当前光标所在的单词（包含点之前的词）
          const textUntilPosition = model.getValueInRange({
            startLineNumber: position.lineNumber,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column,
          });

          // 使用正则提取光标前的最后一个词 (比如 "用户.")
          const match = textUntilPosition.match(/([a-zA-Z\u4e00-\u9fa5]+)\.$/);

          const suggestions = [];

          // 🟢 情况 1：触发了点 (如：用户.)，进入属性下钻模式
          if (match) {
            const objectName = match[1]; // 拿到 "用户"
            const targetObj = qlDictionary.objects[objectName];

            if (targetObj) {
              // 遍历属性，压入提示列表
              for (const [propName, propConfig] of Object.entries(targetObj.properties)) {
                suggestions.push({
                  label: propName, // 提示列表中显示的字
                  kind: monaco.languages.CompletionItemKind.Field, // 图标是个小方块(字段)
                  insertText: propName, // 敲击回车实际插入的字
                  detail: `[属性] ${propConfig.type}`, // 右侧小字
                  documentation: propConfig.desc, // 下方的中文悬浮说明！
                });
              }
            }
            return { suggestions };
          }

          // 🔵 情况 2：日常敲击，提示全局对象、函数和关键字
          // 1. 注入顶级对象 (用户、订单)
          for (const [objName, objConfig] of Object.entries(qlDictionary.objects)) {
            suggestions.push({
              label: objName,
              kind: monaco.languages.CompletionItemKind.Class,
              insertText: objName,
              detail: `[对象]`,
              documentation: objConfig.desc,
            });
          }

          // 2. 注入顶级函数
          qlDictionary.functions.forEach((fn) => {
            suggestions.push({
              label: fn.label,
              kind: monaco.languages.CompletionItemKind.Function,
              // 🌟 史诗级体验：插入后光标自动跳到 ${1} 的位置！
              insertText: fn.insertText,
              insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
              detail: `[内置函数]`,
              documentation: fn.desc,
            });
          });

          // 3. 注入关键字
          qlDictionary.keywords.forEach((kw) => {
            suggestions.push({
              label: kw,
              kind: monaco.languages.CompletionItemKind.Keyword,
              insertText: kw,
            });
          });

          return { suggestions };
        },
      });

      isProviderRegistered = true;
      console.log('🔥 QLExpress 专属语言解释器与智能补全已挂载完毕！');
    }
  };

  // ... 省略常规生命周期和按钮交互逻辑 (与上文一致)
  onMounted(() => {
    const cachedData = localStorage.getItem(CACHE_KEY);
    codeContent.value = cachedData || originalCode.value;
  });
  watch(codeContent, (newVal) => {
    if (!isReadOnly.value) localStorage.setItem(CACHE_KEY, newVal);
  });
  const saveToLocal = () => {
    localStorage.setItem(CACHE_KEY, codeContent.value);
    alert('暂存成功！');
  };
  const handlePreSubmit = () => (showDiffModal.value = true);
  const confirmSubmit = () => {
    alert('发布成功！');
    originalCode.value = codeContent.value;
    localStorage.removeItem(CACHE_KEY);
    showDiffModal.value = false;
  };
  const formatCode = () => {
    if (editorRef.value) editorRef.value.getAction('editor.action.formatDocument').run();
  };
  const toggleReadOnly = () => (isReadOnly.value = !isReadOnly.value);

  const dynamicOptions = computed(() => ({
    automaticLayout: true,
    wordWrap: 'on',
    scrollBeyondLastLine: false,
    minimap: { enabled: false },
    formatOnPaste: true,
    formatOnType: true,
    autoIndent: 'full',
    fontSize: 14,
    fontFamily: "Consolas, 'Courier New', monospace",
    readOnly: isReadOnly.value,
  }));
  const diffOptions = { readOnly: true, automaticLayout: true, renderSideBySide: true };
  const editorRef = shallowRef();
</script>

<style scoped>
  /* 保持原有 iOS 风格... 省略大部分重复代码 */
  .editor-container {
    display: flex;
    flex-direction: column;
    border-radius: 12px;
    overflow: hidden;
    transition: height 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: #1e1e1e;
    position: relative;
  }
  .ios-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    background: rgba(45, 45, 45, 0.75);
    backdrop-filter: saturate(180%) blur(20px);
    -webkit-backdrop-filter: saturate(180%) blur(20px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    z-index: 10;
  }
  .toolbar-left,
  .toolbar-right {
    display: flex;
    gap: 10px;
    align-items: center;
  }
  .divider {
    width: 1px;
    height: 20px;
    background-color: rgba(255, 255, 255, 0.2);
    margin: 0 4px;
  }
  .ios-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
    font-size: 13px;
    font-weight: 500;
    padding: 6px 14px;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    transition: all 0.2s ease;
    outline: none;
  }
  .ios-btn:active {
    transform: scale(0.96);
  }
  .ios-btn:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
  .ios-btn-primary {
    background-color: #007aff;
    color: #ffffff;
  }
  .ios-btn-secondary {
    background-color: rgba(255, 255, 255, 0.15);
    color: #e5e5e5;
  }
  .ios-btn-danger {
    background-color: rgba(255, 59, 48, 0.15);
    color: #ff453a;
  }
  .ios-btn-warning {
    background-color: rgba(255, 149, 0, 0.15);
    color: #ff9f0a;
  }
  .ios-btn-icon {
    background-color: transparent;
    color: #e5e5e5;
    padding: 6px 10px;
  }

  /* 🌟 BUG 终极修复：下拉框 Option 颜色重置 */
  .ios-select {
    appearance: none;
    background-color: rgba(255, 255, 255, 0.1);
    color: #e5e5e5;
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 6px 24px 6px 12px;
    font-size: 13px;
    outline: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23e5e5e5' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right 8px center;
    background-size: 12px;
  }
  /* 👇 修复这里：下拉选项的背景色和文字色强制固定 */
  .ios-select option {
    background-color: #2d2d2d;
    color: #e5e5e5;
  }

  .monaco-box {
    flex: 1;
    width: 100%;
  }

  /* Diff 弹窗样式 (省略) */
  .ios-modal-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(8px);
    z-index: 100;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px;
  }
  .ios-modal-content {
    width: 95%;
    height: 90%;
    background: #1e1e1e;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    animation: modalPop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }
  @keyframes modalPop {
    0% {
      transform: scale(0.95);
      opacity: 0;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }
  .modal-header {
    padding: 16px 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  .modal-header h3 {
    margin: 0;
    color: #fff;
    font-size: 16px;
  }
  .diff-editor-wrapper {
    flex: 1;
    position: relative;
  }
  .modal-footer {
    padding: 16px 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
</style>
