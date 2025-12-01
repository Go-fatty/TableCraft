/**
 * メール通知送信スクリプト
 * @description 指定されたメールアドレスに通知を送信します
 */
function execute(context) {
  const { record } = context;
  
  // メールアドレスの検証
  if (!record.email) {
    return {
      success: false,
      error: 'メールアドレスが指定されていません'
    };
  }
  
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(record.email)) {
    return {
      success: false,
      error: '有効なメールアドレスではありません'
    };
  }
  
  // メール送信処理（実際の実装はバックエンドで行う）
  console.log('Sending email to:', record.email);
  console.log('Subject:', record.subject || 'お知らせ');
  console.log('Body:', record.body || '');
  
  return {
    success: true,
    message: `メールを${record.email}に送信しました`,
    sentTo: record.email,
    timestamp: new Date().toISOString()
  };
}
