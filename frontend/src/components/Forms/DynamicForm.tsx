import React, { useState, useEffect } from 'react';
import './DynamicForm.css';

// 設定ファイルから型定義をインポート
type FormField = {
  name: string;
  type: string;
  label: Record<string, string>;
  placeholder?: Record<string, string>;
  required: boolean;
  readonly: boolean;
  disabled: boolean;
  validation?: any;
  options?: any;
  ui?: {
    hidden?: boolean;
    readonly?: boolean;
    inputType?: string;
  };
  [key: string]: any;
};

type TableConfig = {
  tables: {
    [tableName: string]: {
      name: string;
      label: string;
      columns: FormField[];
    };
  };
  project?: {
    defaultLanguage?: string;
    supportedLanguages: string[];
  };
};

type ValidationConfig = {
  tables: {
    [tableName: string]: {
      fields: {
        [fieldName: string]: {
          rules: Array<{
            type: string;
            value?: any;
            message: string;
          }>;
        };
      };
    };
  };
};

interface DynamicFormProps {
  tableName: string;
  editData?: Record<string, any>;
  onSubmit: (data: Record<string, any>) => void;
  onCancel: () => void;
}

const DynamicForm: React.FC<DynamicFormProps> = ({ 
  tableName, 
  editData, 
  onSubmit, 
  onCancel 
}) => {
  console.log('=== DynamicForm Props ===');
  console.log('tableName:', tableName);
  console.log('editData:', editData);
  console.log('========================');

  const [tableConfig, setTableConfig] = useState<TableConfig | null>(null);
  const [validationConfig, setValidationConfig] = useState<ValidationConfig | null>(null);
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [messages, setMessages] = useState<Record<string, string>>({});
  const [foreignKeyOptions, setForeignKeyOptions] = useState<Record<string, Array<any>>>({});

  // メッセージの解決関数
  const resolveMessage = (key: string, params: any[] = []): string => {
    if (messages[key]) {
      let message = messages[key];
      // パラメータの置換 (例: {0}, {1})
      params.forEach((param, index) => {
        message = message.replace(`{${index}}`, String(param));
      });
      return message;
    }
    return key; // キーが見つからない場合はキーをそのまま返す
  };
  const [submitting, setSubmitting] = useState(false);
  const [language, setLanguage] = useState('ja');

  useEffect(() => {
    loadConfigurations();
  }, [tableName]);

  useEffect(() => {
    if (editData) {
      console.log('Setting editData to formData:', editData);
      // データベースのキーは大文字、フォームフィールドは小文字なので変換
      const normalizedData: Record<string, any> = {};
      Object.keys(editData).forEach(key => {
        // 大文字のキーを小文字に変換
        normalizedData[key.toLowerCase()] = editData[key];
        // 元のキーもそのまま保持（バックワード互換性のため）
        normalizedData[key] = editData[key];
      });
      console.log('Normalized formData:', normalizedData);
      setFormData(normalizedData);
    } else {
      console.log('Clearing formData for new creation');
      setFormData({});
    }
  }, [editData?.id, editData?.ID]); // IDの変化のみを監視

  const loadConfigurations = async () => {
    try {
      setLoading(true);
      setError(null);

      // table-config.json をバックエンドから読み込み
      const tableConfigResponse = await fetch(`http://localhost:8082/api/config/table-config?_t=${Date.now()}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: '{}',
        cache: 'no-cache'
      });
      if (!tableConfigResponse.ok) {
        throw new Error('テーブル設定の読み込みに失敗しました');
      }
      const tableConfigData = await tableConfigResponse.json();
      console.log('DynamicForm - Table config loaded:', tableConfigData);
      setTableConfig(tableConfigData);
      setLanguage(tableConfigData.project?.defaultLanguage || 'ja');

      // validation-config.json をバックエンドから読み込み
      const validationConfigResponse = await fetch('http://localhost:8082/api/config/validation', {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      });
      if (!validationConfigResponse.ok) {
        throw new Error('バリデーション設定の読み込みに失敗しました');
      }
      const validationConfigData = await validationConfigResponse.json();
      setValidationConfig(validationConfigData);

      // 外部キーデータを読み込み
      await loadForeignKeyOptions(tableConfigData);

    } catch (err) {
      setError(err instanceof Error ? err.message : '設定ファイルの読み込みに失敗しました');
      console.error('Failed to load configurations:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadForeignKeyOptions = async (config: TableConfig) => {
    console.log('loadForeignKeyOptions called with tableName:', tableName);
    console.log('Config exists:', !!config);
    console.log('Config.tables exists:', !!config?.tables);
    console.log('Table exists:', !!config?.tables?.[tableName]);
    
    if (!config || !config.tables || !config.tables[tableName]) {
      console.log('Early return - config or table not found');
      return;
    }
    
    const currentTable = config.tables[tableName];
    console.log('Current table:', currentTable.name);
    console.log('Columns:', currentTable.columns);
    
    if (!currentTable.columns || !Array.isArray(currentTable.columns)) {
      console.log('No columns found or not an array');
      return;
    }
    
    const foreignKeyFields = currentTable.columns.filter((field: any) => 
      field.type === 'select' && field.options?.type === 'foreign_key'
    );
    
    console.log('Foreign key fields found:', foreignKeyFields.length);
    
    const options: Record<string, Array<any>> = {};
    
    for (const field of foreignKeyFields) {
      const { table } = field.options;
      try {
        const response = await fetch(`http://localhost:8082/api/config/data/${table}`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.success && data.data) {
            // ページング形式のレスポンスに対応
            const records = data.data.content || data.data;
            options[field.name] = Array.isArray(records) ? records : [];
          }
        }
      } catch (err) {
        console.error(`Failed to load options for ${field.name}:`, err);
        options[field.name] = [];
      }
    }
    
    setForeignKeyOptions(options);
  };

  const handleInputChange = (fieldName: string, value: any) => {
    // Autofill機能を先に実行（非同期）
    if (tableConfig && tableConfig.tables[tableName]) {
      const currentTable = tableConfig.tables[tableName];
      const changedField = currentTable.columns?.find((f: any) => f.name === fieldName);
      
      if (changedField?.autofill?.enabled && value) {
        // Autofillを実行し、完了後にformDataを更新
        handleAutofill(changedField, value).then(() => {
          // Autofill完了後に自動計算を実行
          setFormData(prev => {
            const newData = { ...prev, [fieldName]: value };
            return applyAutoCalculate(newData, fieldName, currentTable);
          });
        });
        return; // Autofillがある場合はここで終了
      }
    }

    // Autofillがない場合は通常の更新
    setFormData(prev => {
      const newData = { ...prev, [fieldName]: value };
      
      // AutoCalculate機能: フィールド変更後に自動計算を実行
      if (tableConfig && tableConfig.tables[tableName]) {
        const currentTable = tableConfig.tables[tableName];
        return applyAutoCalculate(newData, fieldName, currentTable);
      }
      
      return newData;
    });
  };

  // AutoCalculate適用を別関数に分離
  const applyAutoCalculate = (data: Record<string, any>, changedFieldName: string, currentTable: any) => {
    const newData = { ...data };
    
    // 変更されたフィールドが自動計算のトリガーになっているかチェック
    currentTable.columns?.forEach((field: FormField) => {
      if (field.autoCalculate?.enabled) {
        const { triggerFields, formula, targetField } = field.autoCalculate;
        
        // このフィールドがトリガーに含まれているか確認
        if (triggerFields?.includes(changedFieldName)) {
          const calculatedValue = evaluateFormula(formula, newData);
          if (calculatedValue !== null && calculatedValue !== undefined && !isNaN(calculatedValue)) {
            newData[targetField] = calculatedValue;
            console.log(`AutoCalculate: ${targetField} = ${calculatedValue} (formula: ${formula})`);
          } else {
            console.log(`AutoCalculate: Cannot calculate ${targetField} yet (missing values)`);
          }
        }
      }
    });
    
    return newData;
  };

  // 数式を評価する関数
  const evaluateFormula = (formula: string, data: Record<string, any>): number | null => {
    try {
      // 数式内の変数を実際の値に置換
      let expression = formula;
      
      // フィールド名を値に置換
      Object.keys(data).forEach(key => {
        const value = data[key];
        if (value !== null && value !== undefined && value !== '') {
          // 数値に変換可能な場合のみ置換
          const numValue = Number(value);
          if (!isNaN(numValue)) {
            expression = expression.replace(new RegExp(`\\b${key}\\b`, 'g'), String(numValue));
          }
        }
      });
      
      // 全ての変数が置換されたかチェック（アルファベット+アンダースコアが残っていないか）
      if (/[a-zA-Z_]/.test(expression)) {
        // まだ変数が残っている場合は計算しない
        return null;
      }
      
      // 安全な数式のみ評価（数値と演算子のみ許可）
      if (!/^[\d\s+\-*/().]+$/.test(expression)) {
        console.warn('Invalid formula expression:', expression);
        return null;
      }
      
      // Functionを使って安全に評価
      const result = new Function(`return ${expression}`)();
      return typeof result === 'number' ? result : null;
    } catch (error) {
      console.error('Formula evaluation error:', error);
      return null;
    }
  };

  const handleAutofill = async (field: FormField, selectedValue: any): Promise<void> => {
    const { autofill } = field;
    if (!autofill || !autofill.enabled) return;

    try {
      // 選択された外部キーのレコードを取得
      const response = await fetch(
        `http://localhost:8082/api/config/data/${autofill.sourceTable}/${selectedValue}`,
        {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
        }
      );

      if (response.ok) {
        const result = await response.json();
        if (result.success && result.data) {
          const sourceRecord = result.data;
          
          // mappingsに基づいて値を自動設定
          const updates: Record<string, any> = {};
          autofill.mappings.forEach((mapping: any) => {
            const sourceValue = sourceRecord[mapping.from];
            if (sourceValue !== undefined && sourceValue !== null) {
              // overwritableがfalseで既に値が入っている場合はスキップ
              if (!mapping.overwritable && formData[mapping.to]) {
                return;
              }
              updates[mapping.to] = sourceValue;
              console.log(`Autofill: ${mapping.to} = ${sourceValue} (from ${mapping.from})`);
            }
          });

          // 一括更新
          if (Object.keys(updates).length > 0) {
            setFormData(prev => ({
              ...prev,
              ...updates
            }));
          }
        }
      }
    } catch (err) {
      console.error('Autofill failed:', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      // バリデーション実行
      if (validationConfig && validationConfig.tables[tableName]) {
        const fieldValidations = validationConfig.tables[tableName].fields;
        for (const [fieldName, fieldValidation] of Object.entries(fieldValidations)) {
          // 新規作成時はIDフィールドのバリデーションをスキップ
          if (!editData && (fieldName === 'id' || fieldName === 'ID')) {
            continue;
          }
          
          // columnsに含まれていないフィールドはスキップ（hiddenフィールド）
          const fieldInFormFields = tableConfig?.tables[tableName]?.columns?.find((f: FormField) => f.name === fieldName);
          if (!fieldInFormFields) {
            console.log(`Validation skipped for ${fieldName}: not in columns (hidden field)`);
            continue;
          }
          
          // table-configでhidden、readonly、またはautoCalculateに設定されているフィールドはバリデーションスキップ
          const fieldConfig = fieldInFormFields;
          if (fieldConfig) {
            // hiddenフィールドはスキップ
            if (fieldConfig.hidden || fieldConfig.type === 'hidden') {
              console.log(`Validation skipped for ${fieldName}: hidden field`);
              continue;
            }
            // readonlyフィールド（計算フィールドなど）はスキップ
            if (fieldConfig.readonly) {
              console.log(`Validation skipped for ${fieldName}: readonly field`);
              continue;
            }
            // autoCalculateが設定されているフィールドのターゲットフィールドはスキップ
            const isCalculatedField = tableConfig?.tables[tableName]?.columns?.some(
              (f: FormField) => f.autoCalculate?.enabled && f.autoCalculate?.targetField === fieldName
            );
            if (isCalculatedField) {
              console.log(`Validation skipped for ${fieldName}: calculated field`);
              continue;
            }
          }
          
          const value = formData[fieldName];
          for (const rule of fieldValidation.rules) {
            if (!validateField(value, rule)) {
              // メッセージキーを実際のメッセージに解決
              const resolvedMessage = resolveMessage(rule.message, [rule.value]);
              throw new Error(`${fieldName}: ${resolvedMessage}`);
            }
          }
        }
      }

      // id フィールドを除外（自動生成）
      // 大文字小文字の重複も除去
      const { id, ID, ...tempData } = formData;
      const dataToSubmit: Record<string, any> = {};
      
      // 小文字のキーのみを使用（重複除去）
      if (tableConfig && tableConfig.tables[tableName]) {
        const formFields = tableConfig.tables[tableName].columns;
        formFields.forEach((field: any) => {
          const fieldName = field.name;
          if (tempData[fieldName] !== undefined) {
            dataToSubmit[fieldName] = tempData[fieldName];
          }
        });
      }
      
      console.log('Form data before submit:', formData);
      console.log('Data to submit (cleaned):', dataToSubmit);
      await onSubmit(dataToSubmit);
    } catch (error) {
      if (error instanceof Error) {
        alert(error.message);
      }
      console.error('Submit error:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const validateField = (value: any, rule: any): boolean => {
    switch (rule.type) {
      case 'required':
        return value !== null && value !== undefined && value !== '';
      case 'minLength':
        return !value || String(value).length >= rule.value;
      case 'maxLength':
        return !value || String(value).length <= rule.value;
      case 'min':
        return !value || Number(value) >= rule.value;
      case 'max':
        return !value || Number(value) <= rule.value;
      case 'pattern':
        if (rule.value === 'email') {
          return !value || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value));
        }
        // Unicode プロパティエスケープ (\p{L} など) をサポートするために 'u' フラグを追加
        try {
          return !value || new RegExp(rule.value, 'u').test(String(value));
        } catch (e) {
          // 正規表現が無効な場合はフォールバック（'u' フラグなし）
          console.warn('Invalid regex pattern with unicode flag, falling back:', rule.value, e);
          return !value || new RegExp(rule.value).test(String(value));
        }
      default:
        return true;
    }
  };

  const renderFormField = (field: FormField) => {
    const { name, type, label, placeholder, required, readonly, disabled } = field;
    
    // IDフィールドまたはhiddenフィールドは表示しない（編集時のIDフィールドは除く）
    if ((name === 'id' || name === 'ID') && !editData) {
      return null;
    }
    
    // table-config.jsonでhidden=trueに設定されているフィールドは非表示
    if (field.ui?.hidden) {
      return null;
    }
    
    const value = formData[name] !== undefined ? formData[name] : 
                  formData[name.toUpperCase()] !== undefined ? formData[name.toUpperCase()] :
                  (type === 'checkbox' ? false : '');
    const displayLabel = label[language] || label.ja || name;
    const displayPlaceholder = placeholder?.[language] || placeholder?.ja || '';
    
    console.log(`Rendering field ${name}, value:`, value, 'checking keys:', [name, name.toUpperCase()], 'formData:', formData);

    const commonProps = {
      id: name,
      name: name,
      required: required,
      disabled: disabled || readonly,
      value: value,
      onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        let newValue: any = e.target.value;
        
        // 数値型の場合は数値に変換
        if (type === 'number') {
          newValue = newValue === '' ? null : Number(newValue);
        } else if (type === 'checkbox') {
          newValue = (e.target as HTMLInputElement).checked;
        }
        
        handleInputChange(name, newValue);
      }
    };

    switch (type) {
      case 'hidden':
        return (
          <input
            type="hidden"
            id={name}
            name={name}
            value={value || ''}
          />
        );

      case 'text':
        return (
          <input
            {...commonProps}
            type="text"
            className="form-input"
            placeholder={displayPlaceholder}
            maxLength={field.maxLength}
          />
        );

      case 'email':
        return (
          <input
            {...commonProps}
            type="email"
            className="form-input"
            placeholder={displayPlaceholder}
          />
        );

      case 'tel':
        return (
          <input
            {...commonProps}
            type="tel"
            className="form-input"
            placeholder={displayPlaceholder}
          />
        );

      case 'textarea':
        return (
          <textarea
            {...commonProps}
            className="form-textarea"
            rows={field.rows || 4}
            placeholder={displayPlaceholder}
          />
        );

      case 'number':
        return (
          <input
            {...commonProps}
            type="number"
            className="form-input"
            placeholder={displayPlaceholder}
            min={field.min}
            max={field.max}
            step={field.step}
          />
        );

      case 'date':
        return (
          <input
            {...commonProps}
            type="date"
            className="form-input"
          />
        );

      case 'datetime-local':
        return (
          <input
            {...commonProps}
            type="datetime-local"
            className="form-input"
          />
        );

      case 'checkbox':
        return (
          <div className="checkbox-wrapper">
            <input
              type="checkbox"
              id={name}
              name={name}
              checked={!!value}
              disabled={disabled || readonly}
              onChange={(e) => handleInputChange(name, e.target.checked)}
              className="form-checkbox"
            />
            <label htmlFor={name} className="checkbox-label">
              {displayLabel}
            </label>
          </div>
        );

      case 'select':
        const options = field.options?.type === 'foreign_key' 
          ? foreignKeyOptions[name] || []
          : [];
          
        return (
          <select
            {...commonProps}
            className="form-select"
          >
            {field.options?.allowNull && (
              <option value="">
                {field.options.nullLabel?.[language] || field.options.nullLabel?.ja || '選択してください'}
              </option>
            )}
            {field.options?.type === 'foreign_key' ? (
              options.map((option: any) => {
                console.log('Processing option for dropdown:', option, 'field.options:', field.options);
                
                // 大文字小文字両方試してvalueとlabelを取得
                const valueColumn = field.options.valueColumn;
                const displayColumn = field.options.displayColumn;
                
                const optionValue = option[valueColumn] || option[valueColumn?.toUpperCase()] || 
                                  option.id || option.ID;
                const optionLabel = option[displayColumn] || option[displayColumn?.toUpperCase()] || 
                                  option[displayColumn?.toLowerCase()] || optionValue;
                
                console.log(`  valueColumn: ${valueColumn}, displayColumn: ${displayColumn}`);
                console.log(`  optionValue: ${optionValue}, optionLabel: ${optionLabel}`);
                
                return (
                  <option key={optionValue} value={optionValue}>
                    {optionLabel}
                  </option>
                );
              })
            ) : (
              // 静的オプションの場合（今回の設定には含まれていないが将来対応）
              field.options?.items?.map((option: any) => (
                <option key={option.value} value={option.value}>
                  {option.label[language] || option.label.ja || option.value}
                </option>
              ))
            )}
          </select>
        );

      default:
        return (
          <input
            {...commonProps}
            type="text"
            className="form-input"
            placeholder={displayPlaceholder}
          />
        );
    }
  };

  if (loading) {
    return (
      <div className="dynamic-form-container">
        <div className="form-loading">
          <div className="loading-spinner"></div>
          <p>設定読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dynamic-form-container">
        <div className="form-error">
          <h3>❌ エラー</h3>
          <p>{error}</p>
          <button onClick={loadConfigurations} className="retry-button">
            再読み込み
          </button>
        </div>
      </div>
    );
  }

  if (!tableConfig || !tableConfig.tables[tableName]) {
    return (
      <div className="dynamic-form-container">
        <div className="form-error">
          <h3>❌ テーブル設定が見つかりません</h3>
          <p>テーブル「{tableName}」の設定が存在しません。</p>
        </div>
      </div>
    );
  }

  const currentTable = tableConfig.tables[tableName];
  const tableDisplayName = currentTable.label || tableName;
  const formFields = currentTable.columns;

  console.log('=== Form Render Debug ===');
  console.log('tableName:', tableName);
  console.log('currentTable:', currentTable);
  console.log('formFields:', formFields);
  console.log('formData:', formData);
  console.log('editData:', editData);
  console.log('========================');

  return (
    <div className="dynamic-form-container">
      <div className="form-header">
        <h2>
          {editData ? '📝 編集' : '➕ 新規登録'} - {tableDisplayName}
        </h2>
        <p className="form-subtitle">
          必要な情報を入力してください
        </p>
      </div>

      <form onSubmit={handleSubmit} className="dynamic-form">
        <div className="form-fields">
          {formFields.map((field) => {
            console.log('Processing field:', field.name, 'value in formData:', formData[field.name]);
            
            // hiddenフィールドは通常のフォームフィールドとして表示しない
            if (field.type === 'hidden' || field.hidden === true) {
              const renderField = renderFormField(field);
              return renderField ? (
                <div key={field.name} style={{ display: 'none' }}>
                  {renderField}
                </div>
              ) : null;
            }
            
            const renderField = renderFormField(field);
            if (!renderField) return null;

            const displayLabel = field.label[language] || field.label.ja || field.name;

            return (
              <div key={field.name} className="form-field">
                <label htmlFor={field.name} className="form-label">
                  {displayLabel}
                  {field.required && (
                    <span className="required-mark"> *</span>
                  )}
                </label>
                {renderField}
                {field.autoCalculate?.enabled && (
                  <small className="field-info" style={{ color: '#2196F3', fontStyle: 'italic' }}>
                    🧮 自動計算: {field.autoCalculate.formula}
                  </small>
                )}
                {!field.autoCalculate && field.validation && (
                  <small className="field-info">
                    {field.type}
                    {field.maxLength && ` (最大${field.maxLength}文字)`}
                    {field.min !== undefined && field.max !== undefined && ` (${field.min}〜${field.max})`}
                  </small>
                )}
              </div>
            );
          })}
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={onCancel}
            className="cancel-button"
            disabled={submitting}
          >
            キャンセル
          </button>
          <button
            type="submit"
            className="submit-button"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <span className="button-spinner"></span>
                {editData ? '更新中...' : '登録中...'}
              </>
            ) : (
              editData ? '更新' : '登録'
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default DynamicForm;