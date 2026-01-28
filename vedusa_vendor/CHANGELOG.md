# Vendor Application Portal - Changelog

## 2025-11-03 (Update 5) - Status Page English Translation

### Changes Made

**Converted all Chinese content in status.html/status.js to English:**

#### JavaScript Changes (`status.js`)
- ✅ All mock application data translated to English
- ✅ All timeline titles and descriptions in English
- ✅ All review details (strengths, weaknesses, recommendations) in English
- ✅ All function comments translated to English
- ✅ All status labels translated to English
- ✅ All toast messages in English
- ✅ All locations changed to Australian states

**Mock Data Updates:**
- Vendor names: Chinese company names → English names
- Locations: Chinese regions → Australian states (NSW, VIC, QLD, SA, WA, TAS)
- Product categories: Chinese → English
- Timeline items: All titles and descriptions in English
- Review scores: Labels and content in English

**Status Labels:**
- '待审核' → 'Pending'
- '已接收' → 'Received'
- '验证中' → 'Validating'
- '审核中' → 'Under Review'
- '需要面试' → 'Interview Required'
- '补充材料' → 'Additional Info Required'
- '已批准' → 'Approved'
- '已拒绝' → 'Rejected'

**Review Detail Labels:**
- '综合评分' → 'Overall Score'
- '优势' → 'Strengths'
- '需要改进' → 'Areas for Improvement'
- '审核建议' → 'Review Recommendation'
- '满分100分' → 'out of 100'
- '暂无审核详情' → 'No review details available'

**Toast Messages:**
- '请输入申请号' → 'Please enter Application ID'
- '申请号格式不正确' → 'Invalid Application ID format'
- '未找到该申请...' → 'Application not found...'

**Button Text:**
- '查找申请' → 'Search Application'

### Rationale

Status tracking page is now fully in English, consistent with the application form. All mock data uses Australian locations and English vendor names for realistic demonstration.

---

## 2025-11-03 (Update 4) - Full English Validation Messages

### Changes Made

**Converted all validation messages and comments from Chinese to English:**

#### JavaScript Changes (`application.js`)
- ✅ All validation error messages now in English
- ✅ All function comments translated to English
- ✅ All inline comments translated to English
- ✅ Toast notifications in English

**Validation Messages Updated:**
- '供应商名称不能为空' → 'Vendor name is required'
- '请输入有效的PGP签名链接' → 'Please enter a valid PGP public key...'
- '请选择所在地区' → 'Please select your location'
- '请至少选择一种产品类别' → 'Please select at least one product category'
- '请选择预计库存量' → 'Please select estimated stock volume'
- '请选择至少一种主要联系方式' → 'Please select at least one primary contact method'
- '请填写主要联系方式的具体信息' → 'Please fill in primary contact details'
- '请选择至少一种次要联系方式' → 'Please select at least one secondary contact method'
- '请填写次要联系方式的具体信息' → 'Please fill in secondary contact details'
- '请输入有效的邮箱地址' → 'Please enter a valid email address'
- '草稿已保存' → 'Draft saved successfully'
- '加载草稿失败:' → 'Failed to load draft:'

**Summary Labels Updated:**
- '供应商名称：' → 'Vendor Name:'
- '所在地区：' → 'Location:'
- '产品类别：' → 'Product Categories:'
- '主要联系方式：' → 'Primary Contact:'
- '次要联系方式：' → 'Secondary Contact:'

### Rationale

Complete English interface for international vendor portal, ensuring consistency across all user-facing messages and improving accessibility for English-speaking vendors.

---

## 2025-11-03 (Update 3) - Location Changed to Australian States

### Changes Made

**Changed Location options from international regions to Australian states:**

#### HTML Changes (`application.html`)
- Changed label from "Location *" to "Location (State) *"
- Changed placeholder from "Select Location" to "Select State"
- Replaced international region options with Australian states and territories:
  - New South Wales (NSW)
  - Victoria (VIC)
  - Queensland (QLD)
  - South Australia (SA)
  - Western Australia (WA)
  - Tasmania (TAS)
  - Australian Capital Territory (ACT)
  - Northern Territory (NT)

### Rationale

The vendor portal is focused on Australian market, so location selection should reflect Australian states rather than international regions.

---

## 2025-11-03 (Update 2) - PGP Signature Link to PGP Public Key

### Changes Made

**Changed PGP field from URL input to textarea for direct public key input:**

#### HTML Changes (`application.html`)
- Changed "PGP Signature Link" label to "PGP Public Key"
- Changed from `<input type="url">` to `<textarea>` with 8 rows
- Added `font-mono` class for monospace font (better for key display)
- Updated placeholder to show PGP key format example
- Updated description to clarify users should paste their complete PGP public key

#### JavaScript Changes (`application.js`)
- Renamed validation function calls from `validateURL` to `validatePGPKey`
- Replaced `validateURL()` function with new `validatePGPKey()` function
- New validation checks:
  - Verifies PGP key format (must contain BEGIN and END markers)
  - Checks minimum key length (200 characters)
  - Provides clear error messages

### Rationale

1. **Better UX** - Users can directly paste their PGP public key instead of hosting it somewhere
2. **More secure** - Key is submitted directly, no external dependencies
3. **Standard format** - Validates actual PGP key format
4. **Self-contained** - No need for external links that might expire

---

## 2025-11-03 (Update 1) - Wallet Address Fields Removal

### Changes Made

**Removed cryptocurrency wallet address fields from the application form:**

#### HTML Changes (`application.html`)
- Removed BTC Wallet Address input field
- Removed XMR Wallet Address input field
- Removed USDT Wallet Address input field
- Updated PGP Signature description text from "Used for identity verification and wallet address changes" to "Used for identity verification"

**Lines removed:** 176-197 (wallet address input fields section)

#### JavaScript Changes (`application.js`)
- Removed wallet address real-time validation listeners (lines 50-59)
- Removed wallet address validation in `validateStep1()` function (lines 306-313)
- Removed entire `validateWallet()` function (lines 435-464)

### Rationale

Cryptocurrency wallet addresses are now collected **after** a vendor application is approved and the vendor account is created. This change:

1. **Simplifies the application process** - Reduces initial barrier to entry
2. **Improves security** - Wallet addresses are only required when actually needed
3. **Better workflow** - Allows vendors to update payment information in their vendor dashboard after approval

### What Happens Now

**Application Flow:**
1. Vendor submits application (without wallet addresses)
2. Admin reviews and approves application
3. Vendor account is created
4. Vendor can then provide wallet addresses in the Vendor Management backend

### Fields Still Required in Application

- Vendor Name
- Market Experience (optional)
- PGP Signature Link
- Location
- Product Categories
- Stock Volume
- Primary Contact Methods
- Secondary Contact Methods
- Product Description (optional)

### Backend Compatibility

The backend API still accepts wallet address fields for backward compatibility. When the application is approved and a Vendor record is created, the wallet addresses can be added by the admin in the Vendor List management page.

### Testing

To test the updated application form:

```bash
cd /Users/jc/Documents/workshop/medusa-developOS3/vedusa_vendor
python3 -m http.server 8000
# Visit: http://localhost:8000/application.html
```

Verify:
- ✅ No wallet address fields in Step 1
- ✅ PGP Signature field still present
- ✅ Form validation works correctly
- ✅ Can proceed to next steps without wallet addresses
- ✅ Form submission works (frontend only)

### Files Modified

1. `vedusa_vendor/application.html` - Removed wallet input fields
2. `vedusa_vendor/application.js` - Removed wallet validation logic
3. `vedusa_vendor/CHANGELOG.md` - This file (documentation)

### Migration Notes

If you have any existing applications that were submitted with the old form (including wallet addresses), they will still work correctly. The backend can handle both formats.

### Future Enhancements

Consider adding:
- Vendor dashboard where approved vendors can manage their wallet addresses
- API endpoint to update vendor payment information
- Notification system to remind vendors to complete payment setup

