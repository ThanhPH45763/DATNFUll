# Test Voucher Calculation Fix

## Test Cases for Percentage Voucher Calculation

### Test Case 1: Basic Percentage Calculation
- **Input**: Tổng tiền: 500,000đ, Voucher: 20%
- **Expected**: Giảm: 100,000đ, Thành tiền: 400,000đ
- **Old Bug**: Giảm: 20đ, Thành tiền: 499,980đ ❌
- **Fixed**: Giảm: 100,000đ, Thành tiền: 400,000đ ✅

### Test Case 2: Percentage with Maximum Limit
- **Input**: Tổng tiền: 1,000,000đ, Voucher: 50% (tối đa 200,000đ)
- **Expected**: Giảm: 200,000đ, Thành tiền: 800,000đ
- **Calculation**: 1,000,000 × 50% = 500,000đ > 200,000đ → Apply 200,000đ

### Test Case 3: Small Percentage
- **Input**: Tổng tiền: 100,000đ, Voucher: 5%
- **Expected**: Giảm: 5,000đ, Thành tiền: 95,000đ

### Test Case 4: Fixed Amount Voucher (Unchanged)
- **Input**: Tổng tiền: 300,000đ, Voucher: 50,000đ (tiền mặt)
- **Expected**: Giảm: 50,000đ, Thành tiền: 250,000đ

### Test Case 5: Percentage Below Minimum
- **Input**: Tổng tiền: 100,000đ, Voucher: 10% (tối thiểu 200,000đ)
- **Expected**: Không áp dụng voucher

## API Test Commands

```bash
# Test percentage voucher calculation
curl -X POST "http://localhost:8080/banhang/updateTongTienHoaDon?idHoaDon=1"

# Check voucher details
curl -X GET "http://localhost:8080/voucher/findById?id=1"
```

## Verification Steps

1. Start backend application
2. Create test invoice with items
3. Apply percentage voucher
4. Verify calculation in database
5. Check frontend display matches backend calculation

## Files Modified

- ✅ `BanHangController.java` - Fixed percentage calculation logic
- ✅ Added proper RoundingMode import
- ✅ Added voucher type checking

## Status

🎉 **FIX COMPLETED** - Voucher percentage calculation now works correctly!