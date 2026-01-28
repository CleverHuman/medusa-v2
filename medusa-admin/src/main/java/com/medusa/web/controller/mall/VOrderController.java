package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.entity.SysUser;
import com.medusa.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import com.medusa.mall.service.IVOrderService;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.common.annotation.Excel;
import com.medusa.mall.domain.VOrder;
import com.medusa.mall.domain.export.VOrderAccExport;
import com.medusa.mall.domain.export.VOrderPhExport;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

@RestController
@RequestMapping("/admin/mall/vorder")
public class VOrderController extends BaseController {
    @Autowired
    private IVOrderService vOrderService;

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map<String, Object> params) {
        startPage();
        // Apply vendor_id filter for warehouse staff
        applyVendorFilter(params);
        
        // ✅ 处理日期范围参数：Spring 的 @RequestParam 会将 params[beginTime] 转换为扁平化的键
        // 需要手动将这些参数重新组织为嵌套的 params Map
        Map<String, Object> nestedParams = new java.util.HashMap<>();
        boolean hasDateParams = false;
        
        // 检查是否有 params[beginTime] 或 params.beginTime 格式的参数
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("params[") && key.endsWith("]")) {
                // 处理 params[beginTime] 格式
                String nestedKey = key.substring(7, key.length() - 1); // 提取 beginTime 或 endTime
                nestedParams.put(nestedKey, entry.getValue());
                hasDateParams = true;
            } else if (key.equals("params") && entry.getValue() instanceof Map) {
                // 处理已经嵌套的 params 对象
                @SuppressWarnings("unchecked")
                Map<String, Object> existingParams = (Map<String, Object>) entry.getValue();
                nestedParams.putAll(existingParams);
                hasDateParams = true;
            }
        }
        
        // 如果没有找到嵌套参数，创建一个空的 Map
        if (!hasDateParams) {
            nestedParams = new java.util.HashMap<>();
        }
        
        // 将嵌套的 params 放入参数 Map
        params.put("params", nestedParams);
        
        List<Map<String, Object>> list = vOrderService.selectVOrderList(params);
        return getDataTable(list);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        // Apply vendor_id filter for warehouse staff
        applyVendorFilter(params);
        List<Map<String, Object>> list = vOrderService.selectVOrderList(params);
        List<VOrder> vOrderList = new ArrayList<>();
        
        for (Map<String, Object> map : list) {
            VOrder vOrder = new VOrder();
            // 手动映射字段，添加空值检查
            vOrder.setOrderSn(getStringValue(map, "order_sn"));
            vOrder.setUsername(getStringValue(map, "username"));
            vOrder.setProcode(getStringValue(map, "procode"));
            vOrder.setAmt(getStringValue(map, "amt"));
            vOrder.setQty(getStringValue(map, "qty"));
            vOrder.setPostage(getStringValue(map, "postage"));
            vOrder.setReceiverName(getStringValue(map, "receiver_name"));
            vOrder.setAddressLine1(getStringValue(map, "address_line1"));
            vOrder.setAddressLine2(getStringValue(map, "address_line2"));
            vOrder.setAddressLine3(getStringValue(map, "address_line3"));
            vOrder.setCity(getStringValue(map, "city"));
            vOrder.setState(getStringValue(map, "state"));
            vOrder.setPostCode(getStringValue(map, "post_code"));
            vOrder.setTcoin(getStringValue(map, "tcoin"));
            vOrder.setPaidcoin(getStringValue(map, "paidcoin"));
            vOrder.setTamt(getStringValue(map, "tamt"));
            vOrder.setCointype(getStringValue(map, "cointype"));
            vOrder.setStatus(getIntegerValue(map, "status"));
            vOrderList.add(vOrder);
        }
        
        ExcelUtil<VOrder> util = new ExcelUtil<>(VOrder.class);
        util.exportExcel(response, vOrderList, "Order Data");
    }

    @GetMapping("/export/acc")
    public void exportAcc(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        // Apply vendor_id filter for warehouse staff
        applyVendorFilter(params);
        
        // ✅ 强制设置为 Shipped 状态（status = 3）
        params.put("status", 3);
        
        List<Map<String, Object>> list = vOrderService.selectVOrderList(params);
        List<VOrderAccExport> exportList = new ArrayList<>();
        
        for (Map<String, Object> map : list) {
            String orderSn = getStringValue(map, "order_sn");
            String username = getStringValue(map, "username");
            String paidcoin = getStringValue(map, "paidcoin");
            String cointype = getStringValue(map, "cointype");
            
            // ✅ 获取 Date
            String createTime = getStringValue(map, "create_time");
            String date = "";
            if (createTime != null && !createTime.trim().isEmpty()) {
                try {
                    // 格式化日期为 yyyy-MM-dd
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date dateObj = inputFormat.parse(createTime);
                    date = outputFormat.format(dateObj);
                } catch (Exception e) {
                    // 如果解析失败，尝试其他格式或使用原始值
                    try {
                        java.text.SimpleDateFormat inputFormat2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        java.text.SimpleDateFormat outputFormat2 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date dateObj2 = inputFormat2.parse(createTime);
                        date = outputFormat2.format(dateObj2);
                    } catch (Exception e2) {
                        date = createTime; // 如果都失败，使用原始值
                    }
                }
            }
            
            // ✅ 获取 Postage 和 Shipping Cost
            // 只使用 postage 字段（代码），与 Admin Panel 列表显示保持一致
            String postage = getStringValue(map, "postage");
            if (postage == null || postage.trim().isEmpty()) {
                postage = "";
            }
            
            String freightAmountStr = getStringValue(map, "freight_amount");
            String shippingCost = freightAmountStr != null && !freightAmountStr.trim().isEmpty() 
                ? freightAmountStr : "0.00";

            // ✅ 获取 Comment (remark)
            String comment = getStringValue(map, "remark");
            if (comment == null || comment.trim().isEmpty()) {
                comment = "";
            }

            String[] procodes = splitBySemicolon(getStringValue(map, "procode"));
            String[] amts = splitBySemicolon(getStringValue(map, "amt"));  // ✅ Product Amount (产品型号)
            String[] qtys = splitBySemicolon(getStringValue(map, "qty"));
            String[] tcoins = splitBySemicolon(getStringValue(map, "tcoin"));

            // ✅ 计算每个产品的 Paid Coin（按 Total Coin 比例分配）
            String[] paidcoins = new String[tcoins.length];
            try {
                // 解析订单总的 Paid Coin
                double totalPaidCoin = paidcoin != null && !paidcoin.trim().isEmpty() 
                    ? Double.parseDouble(paidcoin) : 0.0;
                
                if (totalPaidCoin > 0 && tcoins.length > 0) {
                    // Step 1: 计算所有产品的 Total Coin 总和
                    double totalCoinSum = 0.0;
                    double[] tcoinValues = new double[tcoins.length];
                    for (int i = 0; i < tcoins.length; i++) {
                        try {
                            tcoinValues[i] = Double.parseDouble(tcoins[i].trim());
                            totalCoinSum += tcoinValues[i];
                        } catch (NumberFormatException e) {
                            tcoinValues[i] = 0.0;
                        }
                    }
                    
                    // Step 2: 按比例分配 Paid Coin 给每个产品
                    if (totalCoinSum > 0) {
                        for (int i = 0; i < tcoins.length; i++) {
                            double percentage = tcoinValues[i] / totalCoinSum;
                            double itemPaidCoin = totalPaidCoin * percentage;
                            paidcoins[i] = String.format("%.8f", itemPaidCoin);
                        }
                    } else {
                        // 如果 Total Coin 总和为 0，所有产品的 Paid Coin 都设为 0
                        for (int i = 0; i < tcoins.length; i++) {
                            paidcoins[i] = "0.00000000";
                        }
                    }
                } else {
                    // 如果没有 Paid Coin，所有产品都设为 0
                    for (int i = 0; i < tcoins.length; i++) {
                        paidcoins[i] = "0.00000000";
                    }
                }
            } catch (Exception e) {
                // 如果计算出错，将所有 Paid Coin 设为原值或 0
                for (int i = 0; i < tcoins.length; i++) {
                    paidcoins[i] = (i == 0 && paidcoin != null) ? paidcoin : "0.00000000";
                }
            }

            int max = Math.max(procodes.length, Math.max(amts.length, qtys.length));
            for (int i = 0; i < Math.max(1, max); i++) {
                VOrderAccExport vo = new VOrderAccExport();
                
                // ✅ 按照新的列顺序设置字段
                vo.setDate(i == 0 ? date : "");  // 只在第一行显示日期
                vo.setOrderSn(orderSn);
                vo.setUsername(username);
                vo.setProcode(i < procodes.length ? procodes[i] : "");
                vo.setProductAmount(i < amts.length ? amts[i] : "");  // ✅ Product Amount = 产品型号（product_spec）
                vo.setQty(i < qtys.length ? qtys[i] : "");
                vo.setTcoin(i < tcoins.length ? tcoins[i] : "");
                vo.setPaidcoin(i < paidcoins.length ? paidcoins[i] : "0.00000000");  // ✅ 每个产品显示分配的 Paid Coin
                vo.setCointype(cointype);
                vo.setPostage(i == 0 ? postage : "");  // 只在第一行显示 Postage
                vo.setShippingCost(i == 0 ? shippingCost : "");  // 只在第一行显示 Shipping Cost
                vo.setComment(i == 0 ? comment : "");  // 只在第一行显示 Comment
                
                exportList.add(vo);
            }
        }
        
        ExcelUtil<VOrderAccExport> util = new ExcelUtil<>(VOrderAccExport.class);
        util.exportExcel(response, exportList, "orders_acc");
    }

    @GetMapping("/export/ph")
    public void exportPh(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        // Apply vendor_id filter for warehouse staff
        applyVendorFilter(params);
        List<Map<String, Object>> list = vOrderService.selectVOrderList(params);
        List<VOrderPhExport> exportList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            String orderSn = getStringValue(map, "order_sn");
            String username = getStringValue(map, "username");
            String postage = getStringValue(map, "postage");

            // Keep products grouped in one row, use newline to separate multiple products
            // Convert semicolon-separated values to newline-separated values (like in the order list)
            String procodeStr = getStringValue(map, "procode");
            String amtStr = getStringValue(map, "amt");
            String qtyStr = getStringValue(map, "qty");

            // Replace semicolons (with optional spaces) with newlines to match the order list display
            // Handle both "; " and ";" patterns
            String procode = procodeStr.replaceAll(";\\s*", "\n").trim();
            String amt = amtStr.replaceAll(";\\s*", "\n").trim();
            String qty = qtyStr.replaceAll(";\\s*", "\n").trim();

            VOrderPhExport vo = new VOrderPhExport();
            vo.setOrderSn(orderSn);
            vo.setUsername(username);
            vo.setProcode(procode);
            vo.setAmt(amt);
            vo.setQty(qty);
            vo.setPostage(postage);

            String receiver = buildReceiverExactAsList(
                getStringValue(map, "receiver_name"),
                getStringValue(map, "address_line1"),
                getStringValue(map, "address_line2"),
                getStringValue(map, "address_line3"),
                getStringValue(map, "city"),
                getStringValue(map, "state"),
                getStringValue(map, "post_code")
            );
            vo.setReceiver(receiver);
            exportList.add(vo);
        }
        
        // Custom export with auto filter
        ExcelUtil<VOrderPhExport> util = new ExcelUtil<>(VOrderPhExport.class);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        util.init(exportList, "orders_ph", "", Excel.Type.EXPORT);
        util.writeSheet();
        
        // Set auto filter to enable sorting
        try {
            Sheet sheet = util.getSheet();
            if (sheet != null && exportList.size() > 0) {
                int lastRowNum = sheet.getLastRowNum();
                int lastColNum = 6; // Order Number, Username, Product Code, Amount, Quantity, Postage, Receiver Name
                if (lastRowNum > 0) {
                    sheet.setAutoFilter(new CellRangeAddress(0, lastRowNum, 0, lastColNum));
                }
            }
        } catch (Exception e) {
            // Log error but continue with export
            System.err.println("Failed to set auto filter: " + e.getMessage());
        }
        
        // Write to response
        try {
            util.getWorkbook().write(response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write Excel file", e);
        }
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String buildReceiver(String name, String line1, String line2, String line3, String city, String state, String postCode) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            sb.append(name.trim());
        }
        if (StringUtils.isNotBlank(line1)) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(line1.trim());
        }
        if (StringUtils.isNotBlank(line2)) {
            sb.append('\n').append(line2.trim());
        }
        if (StringUtils.isNotBlank(line3)) {
            sb.append('\n').append(line3.trim());
        }
        String lastLine = (StringUtils.defaultString(city).trim() + ", " + StringUtils.defaultString(state).trim() + " " + StringUtils.defaultString(postCode).trim());
        if (StringUtils.isNotBlank(lastLine)) {
            sb.append('\n').append(lastLine);
        }
        return sb.toString();
    }

    // Match admin list exactly: always show "city, state postCode" even if city is empty
    private String buildReceiverExactAsList(String name, String line1, String line2, String line3, String city, String state, String postCode) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(name)) sb.append(name);
        if (StringUtils.isNotEmpty(line1)) { if (sb.length() > 0) sb.append('\n'); sb.append(line1); }
        if (StringUtils.isNotEmpty(line2)) { sb.append('\n').append(line2); }
        if (StringUtils.isNotEmpty(line3)) { sb.append('\n').append(line3); }
        String lastLine = (StringUtils.defaultString(city) + ", " + StringUtils.defaultString(state) + " " + StringUtils.defaultString(postCode));
        sb.append('\n').append(lastLine);
        return sb.toString();
    }

    private String[] splitBySemicolon(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new String[0];
        }
        String[] parts = value.split(";");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i] == null ? "" : parts[i].trim();
        }
        return parts;
    }

    /**
     * Apply vendor_id filter for warehouse staff users
     */
    private void applyVendorFilter(Map<String, Object> params) {
        try {
            // Get current logged in user
            SysUser currentUser = SecurityUtils.getLoginUser().getUser();
            
            // Check if user has vendor_id (only warehouse staff accounts have this field set)
            // If vendor_id is not null, filter orders by this vendor_id
            if (currentUser != null && currentUser.getVendorId() != null) {
                params.put("vendorId", currentUser.getVendorId());
            }
        } catch (Exception e) {
            // If error occurs (e.g., user not logged in), continue without filter
            // This allows admin users to see all orders
        }
    }
} 