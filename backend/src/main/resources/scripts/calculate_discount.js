/**
 * 割引計算スクリプト
 * @description 商品価格から10%の割引額を計算します
 */
function execute(context) {
  const { record } = context;
  
  // 価格の検証
  if (!record.price || record.price < 0) {
    return {
      success: false,
      error: '有効な価格が指定されていません'
    };
  }
  
  // 10%割引を計算
  const discount = record.price * 0.1;
  const finalPrice = record.price - discount;
  
  return {
    success: true,
    discount: discount,
    finalPrice: finalPrice,
    message: `割引額: ${discount}円、割引後価格: ${finalPrice}円`
  };
}
