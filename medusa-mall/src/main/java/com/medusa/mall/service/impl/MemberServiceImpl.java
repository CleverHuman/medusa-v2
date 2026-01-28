package com.medusa.mall.service.impl;

import com.medusa.common.constant.UserConstants;
import com.medusa.common.core.domain.entity.Member;
import com.medusa.common.core.domain.model.LoginMember;
import com.medusa.common.exception.ServiceException;
import com.medusa.common.exception.user.UserNotExistsException;
import com.medusa.common.exception.user.UserPasswordNotMatchException;
import com.medusa.common.utils.SecurityUtils;
import com.medusa.common.utils.ServletUtils;
import com.medusa.common.utils.StringUtils;
import com.medusa.framework.web.service.MemberTokenService;
import com.medusa.mall.domain.member.MemberInfo;
import com.medusa.mall.domain.member.MemberLevel;
import com.medusa.mall.domain.member.MemberLoginInfo;
import com.medusa.mall.domain.member.MemberPointHistory;
import com.medusa.mall.domain.member.OrderHistoryVO;
import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.order.OrderItem;
import com.medusa.mall.domain.order.ShippingAddress;
import com.medusa.mall.domain.order.Payment;
import com.medusa.mall.service.PaymentService;
import com.medusa.mall.mapper.MemberMapper;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.mapper.OrderItemMapper;
import com.medusa.mall.mapper.ShippingAddressMapper;
import com.medusa.mall.mapper.MemberBenefitMapper;
import com.medusa.mall.mapper.member.MemberPointHistoryMapper;
import com.medusa.mall.service.IAkSkService;
import com.medusa.mall.service.IMemberLevelService;
import com.medusa.mall.service.IMemberLoginInfoService;
import com.medusa.mall.service.IMemberService;
import com.medusa.mall.service.member.IMemberPointHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MemberServiceImpl implements IMemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberTokenService memberTokenService;

    @Autowired
    private IAkSkService akSkService;

    @Autowired
    private IMemberLevelService memberLevelService;

    @Autowired
    private IMemberLoginInfoService memberLoginInfoService;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShippingAddressMapper shippingAddressMapper;

    @Autowired
    private MemberBenefitMapper memberBenefitMapper;

    @Autowired
    private MemberPointHistoryMapper memberPointHistoryMapper;

    @Autowired
    private IMemberPointHistoryService memberPointHistoryService;

    @Autowired
    @Lazy
    private PaymentService paymentService;

    @Autowired
    private com.medusa.mall.service.member.IMemberPcspService memberPcspService;

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Override
    public String osLogin(Member member){
        String username = member.getUsername();
        String password = member.getPassword();
        Member queryMember = memberMapper.selectMemberByUsername(username);
        if (queryMember == null) {
            throw new UserPasswordNotMatchException();
        }else {
            // Check if account is suspended
            if (queryMember.getStatus() != null && queryMember.getStatus() == 2) {
                MemberLoginInfo memberLoginInfo = new MemberLoginInfo();
                memberLoginInfo.setMemberId(queryMember.getMemberId());
                memberLoginInfo.setSourceType(0);
                memberLoginInfo.setMsg("account suspended");
                memberLoginInfoService.insertMemberLoginInfo(memberLoginInfo);
                throw new ServiceException("Alert! Your account has been suspended. Please contact customer support to resolve this issue.");
            }
            
            MemberLoginInfo memberLoginInfo = new MemberLoginInfo();
            memberLoginInfo.setMemberId(queryMember.getMemberId());
            memberLoginInfo.setSourceType(0);
            String queryPassword = queryMember.getPassword();
            if (!SecurityUtils.matchesPassword(password, queryPassword)){
                memberLoginInfo.setMsg("password error");
                memberLoginInfoService.insertMemberLoginInfo(memberLoginInfo);
                throw new UserPasswordNotMatchException();
            }
            else{
                Long newMemberId= queryMember.getMemberId();
                String newUsername = queryMember.getUsername();
                Member newMember = new Member();
                newMember.setMemberId(newMemberId);
                newMember.setUsername(newUsername);
                LoginMember loginMember = new LoginMember(newMemberId, newMember);
                String token = memberTokenService.createToken(loginMember);
                memberLoginInfo.setMsg("login success");
                memberLoginInfoService.insertMemberLoginInfo(memberLoginInfo);
                return token;
            }
        }
    }

    @Override
    public String tgLogin(Member member, HttpServletRequest request){
        Boolean auth = akSkService.checkAkSkAuth(request);
        String tgId = member.getTgId();
        Member queryMember = memberMapper.selectMemberByTgId(tgId);
        if (queryMember == null) {
            throw new UserNotExistsException();
        }else {
            // Check if account is suspended
            if (queryMember.getStatus() != null && queryMember.getStatus() == 2) {
                MemberLoginInfo memberLoginInfo = new MemberLoginInfo();
                memberLoginInfo.setMemberId(queryMember.getMemberId());
                memberLoginInfo.setSourceType(1);
                memberLoginInfo.setMsg("account suspended");
                memberLoginInfoService.insertMemberLoginInfo(memberLoginInfo);
                throw new ServiceException("Alert! Your account has been suspended. Please contact customer support to resolve this issue.");
            }
            
            MemberLoginInfo memberLoginInfo = new MemberLoginInfo();
            memberLoginInfo.setMemberId(queryMember.getMemberId());
            memberLoginInfo.setSourceType(1);
            Long newMemberId= queryMember.getMemberId();
            String newUsername = queryMember.getUsername();
            Member newMember = new Member();
            newMember.setMemberId(newMemberId);
            newMember.setUsername(newUsername);
            LoginMember loginMember = new LoginMember(newMemberId, newMember);
            String token = memberTokenService.createToken(loginMember);
            memberLoginInfo.setMsg("login success");
            memberLoginInfoService.insertMemberLoginInfo(memberLoginInfo);
            return token;
        }
    }


    @Override
    public String register(Member member){
        Integer sourceType = member.getSourceType();
        if (sourceType == 0) {
            return os_register(member);
        }else if (sourceType == 1) {
            return tg_register(member);
        }else{
            return "do not support the source";
        }
    }

    private String os_register(Member member){
        String msg = "", username = member.getUsername(), password = member.getPassword();
        String PrimaryContact = member.getPrimaryContact();
        String SecondaryContact = member.getSecondaryContact();

        Member addMember = new Member();
        addMember.setUsername(username);

        if (StringUtils.isEmpty(username))
        {
            msg = "need username";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "need password";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = String.format("username must between %d and %d characters long",UserConstants.USERNAME_MIN_LENGTH,UserConstants.USERNAME_MAX_LENGTH);
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = String.format("password must between %d and %d characters long",UserConstants.PASSWORD_MIN_LENGTH,UserConstants.PASSWORD_MAX_LENGTH);
        }
        else if (!checkUsernameUnique(addMember))
        {
            msg = String.format("username %s already exist",username);
        }
        else if (StringUtils.isEmpty(PrimaryContact)){
            msg = "need PrimaryContact";
        }
        else
        {
            addMember.setPrimaryContact(PrimaryContact);
            addMember.setSecondaryContact(StringUtils.isEmpty(SecondaryContact) ? "" : SecondaryContact);
            addMember.setPassword(SecurityUtils.encryptPassword(password));
            int regFlag = insertMember(addMember);
            MemberLevel addMemberLevel = new MemberLevel();
            addMemberLevel.setMemberId(addMember.getMemberId());
            memberLevelService.insertMemberLevel(addMemberLevel);
            if (regFlag != 1)
            {
                msg = "register failed,please contact admin";
            }
        }
        return msg;
    }


    private String tg_register(Member member){
        String msg = "", tgId = member.getTgId(),tgUsername = member.getTgUsername();
        
        // 正常用户注册：添加@前缀
        tgUsername = "@" + tgUsername;
        
        Member addMember = new Member();
        addMember.setTgId(tgId);
        if (StringUtils.isEmpty(tgId))
        {
            msg = "need tgId";
        }
        else if (StringUtils.isEmpty(tgUsername))
        {
            msg = "need tgUsername";
        }
        else if (!checkTgIdUnique(addMember))
        {
            msg = String.format("tgId %s already exist",tgId);
        }else{
            addMember.setUsername(tgUsername);
            addMember.setPrimaryContact(tgUsername);
            addMember.setSourceType(1);
            addMember.setTgUsername(tgUsername);
            // 移除isGuest设置，因为现在使用临时用户策略
            int regFlag = insertMember(addMember);
            MemberLevel addMemberLevel = new MemberLevel();
            addMemberLevel.setMemberId(addMember.getMemberId());
            memberLevelService.insertMemberLevel(addMemberLevel);
            if (regFlag != 1)
            {
                msg = "register failed,please contact admin";
            }
        }

        return msg;
    }


    @Override
    public boolean checkUsernameUnique(Member member) {
        Long memberId = StringUtils.isNull(member.getMemberId()) ? -1L : member.getMemberId();
        Member info = memberMapper.checkUsernameUnique(member.getUsername());
        if (StringUtils.isNotNull(info) && info.getMemberId().longValue() != memberId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


    public boolean checkTgIdUnique(Member member) {
        Long memberId = StringUtils.isNull(member.getMemberId()) ? -1L : member.getMemberId();
        Member info = memberMapper.checkTgIdUnique(member.getTgId());
        if (StringUtils.isNotNull(info) && info.getMemberId().longValue() != memberId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public int insertMember(Member member) {
        return memberMapper.insertMember(member);
    }

    @Override
    public int updateMember(Member member) {
        return memberMapper.updateMember(member);
    }

    @Override
    public List<MemberInfo> selectMemberList(MemberInfo memberInfo) {
        return memberMapper.selectMemberList(memberInfo);
    }

    @Override
    public MemberInfo selectMemberInfoById(Long id) {
        MemberInfo memberInfo = memberMapper.selectMemberInfoById(id);
        
        // 查询并填充PCSP信息
        if (memberInfo != null) {
            try {
                // 首先查询当前账号的PCSP
                com.medusa.mall.domain.member.MemberPcsp activePcsp = 
                    memberPcspService.selectActivePcspByMemberId(id);
                
                // 如果当前账号没有PCSP，且有关联账号，则查询关联账号的PCSP
                if ((activePcsp == null || !activePcsp.isValid()) && memberInfo.getLinkedAccount() != null) {
                    log.debug("Checking PCSP for linked account: {}", memberInfo.getLinkedAccount());
                    activePcsp = memberPcspService.selectActivePcspByMemberId(memberInfo.getLinkedAccount());
                }
                
                if (activePcsp != null && activePcsp.isValid()) {
                    memberInfo.setPcspStatus(1); // 有效
                    memberInfo.setPcspExpiryDate(activePcsp.getExpiryDate());
                } else {
                    memberInfo.setPcspStatus(0); // 未购买或已过期
                    memberInfo.setPcspExpiryDate(null);
                }
            } catch (Exception e) {
                log.error("Failed to fetch PCSP info for member: {}", id, e);
                memberInfo.setPcspStatus(0);
                memberInfo.setPcspExpiryDate(null);
            }
        }
        
        return memberInfo;
    }

    @Override
    public Member selectMemberByTgId(Long tgId) {
        return memberMapper.selectMemberByTgId(tgId.toString());
    }

    @Override
    public int suspendMemberByIds(Long[] ids) {
        return memberMapper.updateMemberStatusByIds(2, ids);
    }

    public int resumeMemberByIds(Long[] ids) {
        return memberMapper.updateMemberStatusByIds(0, ids);
    }

    @Override
    public int deleteMemberByIds(Long[] ids) {
        // Hard delete - physically remove from database
        return memberMapper.deleteMemberByIds(ids);
    }

    @Override
    public Member selectMemberInfoByUsername(String username) {
        // 根据用户名查询会员基本信息
        return memberMapper.selectMemberByUsername(username);
    }

    @Override
    public Member selectMemberByUsername(String username) {
        // 根据用户名查询会员基本信息
        return memberMapper.selectMemberByUsername(username);
    }

    @Override
    public int updateMemberRemark(Member member) {
        return memberMapper.updateMemberRemark(member);
    }

    @Override
    public List<OrderHistoryVO> getMemberOrderHistory(Long memberId) {

        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId2 = loginMember.getMemberId();
        List<Order> orders = orderMapper.selectOrderListById(memberId2);
        List<OrderHistoryVO> result = new java.util.ArrayList<>();
        for (Order order : orders) {
            OrderHistoryVO vo = new OrderHistoryVO();
            vo.setOrderId(Long.valueOf(order.getOrderSn()));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
            vo.setOrderDate(sdf.format(order.getCreateTime()));
            // 取第一个商品
            List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getId());
            if (items != null && !items.isEmpty()) {
                vo.setProductName(items.get(0).getProductName());
                vo.setAmount(items.get(0).getProductSpec());
            } else {
                vo.setProductName("-");
                vo.setAmount("-");
            }
            // 查物流单号
            ShippingAddress addr = shippingAddressMapper.selectShippingAddressByOrderId(order.getId());
            if (addr != null && addr.getShippingNumber() != null && !addr.getShippingNumber().isEmpty()) {
                vo.setTrackingNo(addr.getShippingNumber());
            } else {
                vo.setTrackingNo("Not Available");
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<OrderHistoryVO> getMemberOrderHistoryPaged(int page) {
        LoginMember loginMember = memberTokenService.getLoginMember(ServletUtils.getRequest());
        Long memberId = loginMember.getMemberId();
        int pageSize = 5;
        int offset = (page - 1) * pageSize;
        List<Order> orders = orderMapper.selectOrderListByIdPaged(memberId, offset, pageSize);
        List<OrderHistoryVO> result = new java.util.ArrayList<>();
        for (Order order : orders) {
            OrderHistoryVO vo = new OrderHistoryVO();
            vo.setOrderId(Long.valueOf(order.getOrderSn()));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
            vo.setOrderDate(sdf.format(order.getCreateTime()));
            
            // 获取所有订单项
            List<OrderItem> items = orderItemMapper.selectOrderItemsByOrderId(order.getId());
            if (items != null && !items.isEmpty()) {
                // 设置第一个商品信息用于向后兼容
                vo.setProductName(items.get(0).getProductName());
                vo.setAmount(items.get(0).getProductSpec());
                
                // 设置所有订单项信息
                List<OrderHistoryVO.OrderItemVO> itemVOs = new java.util.ArrayList<>();
                for (OrderItem item : items) {
                    OrderHistoryVO.OrderItemVO itemVO = new OrderHistoryVO.OrderItemVO();
                    itemVO.setProductName(item.getProductName());
                    itemVO.setProductSpec(item.getProductSpec());
                    itemVO.setPrice(item.getPrice() != null ? item.getPrice().toString() : "0");
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().toString() : "0");
                    itemVOs.add(itemVO);
                }
                vo.setItems(itemVOs);
            } else {
                vo.setProductName("-");
                vo.setAmount("-");
                vo.setItems(new java.util.ArrayList<>());
            }
            
            // 获取订单状态
            vo.setOrderStatus(order.getStatus());

            // 获取支付状态
            Payment payment = paymentService.getPaymentByOrderId(order.getId());
            if (payment != null) {
                vo.setPaymentStatus(payment.getPayStatus());
            } else {
                vo.setPaymentStatus(0); // 默认未支付
            }
            
            // 设置Vendor ID（如果有）
            if (order.getVendorId() != null) {
                vo.setVendorId(order.getVendorId());
            }
            
            // 查物流单号和发货时间
            ShippingAddress addr = shippingAddressMapper.selectShippingAddressByOrderId(order.getId());
            if (addr != null) {
                // 设置tracking number（前端会根据等级和PCSP状态决定是否显示）
                if (addr.getShippingNumber() != null && !addr.getShippingNumber().isEmpty()) {
                    vo.setTrackingNo(addr.getShippingNumber());
                } else {
                    vo.setTrackingNo("Not Available");
                }
                
                // 设置发货时间（用于前端判断是否可以显示tracking）
                if (addr.getShippingTime() != null) {
                    java.text.SimpleDateFormat sdfShipping = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    vo.setShippingTime(sdfShipping.format(addr.getShippingTime()));
                }
            } else {
                vo.setTrackingNo("Not Available");
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Member> searchMembersByUsername(String username) {
        return memberMapper.searchMembersByUsername(username);
    }

    @Override
    @Transactional
    public int updateLinkedAccount(Long memberId, Long linkedAccountId) {
        // 双向关联：两个账号都设置对方的ID
        Map<String, Object> params1 = new HashMap<>();
        params1.put("memberId", memberId);
        params1.put("linkedAccountId", linkedAccountId);
        params1.put("updateBy", "admin");
        
        Map<String, Object> params2 = new HashMap<>();
        params2.put("memberId", linkedAccountId);
        params2.put("linkedAccountId", memberId);
        params2.put("updateBy", "admin");
        
        // 先清除两个账号的现有关联
        memberMapper.removeLinkedAccount(memberId);
        memberMapper.removeLinkedAccount(linkedAccountId);
        
        // 设置双向关联
        int result1 = memberMapper.updateLinkedAccount(params1);
        int result2 = memberMapper.updateLinkedAccount(params2);
        
        // 返回总的更新结果
        return result1 + result2;
    }

    @Override
    @Transactional
    public int removeLinkedAccount(Long memberId) {
        // 先获取当前账号的关联账号ID
        Member member = memberMapper.selectMemberByMemberId(memberId);
        if (member != null && member.getLinkedAccount() != null) {
            Long linkedAccountId = member.getLinkedAccount();
            
            // 双向清除：清除两个账号的关联关系
            int result1 = memberMapper.removeLinkedAccount(memberId);
            int result2 = memberMapper.removeLinkedAccount(linkedAccountId);
            
            return result1 + result2;
        }
        
        // 如果没有关联账号，只清除当前账号
        return memberMapper.removeLinkedAccount(memberId);
    }

    @Override
    public Member selectMemberByLinkedAccount(Long linkedAccountId) {
        return memberMapper.selectMemberByLinkedAccount(linkedAccountId);
    }

    @Override
    public Member selectMemberByMemberId(Long memberId) {
        return memberMapper.selectMemberByMemberId(memberId);
    }

    @Override
    @Transactional
    public Map<String, Object> mergeAccounts(Long memberId, Long linkedAccountId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取两个账号的会员等级信息
            MemberLevel mainMemberLevel = memberLevelService.selectMemberLevelByMemberId(memberId);
            MemberLevel linkedMemberLevel = memberLevelService.selectMemberLevelByMemberId(linkedAccountId);
            
            if (mainMemberLevel == null || linkedMemberLevel == null) {
                result.put("success", false);
                result.put("message", "One or both member levels not found");
                return result;
            }
            
            // 2. 计算合并后的积分和订单数
            BigDecimal mainPoints = mainMemberLevel.getCurrentPoint() != null ? mainMemberLevel.getCurrentPoint() : BigDecimal.ZERO;
            BigDecimal linkedPoints = linkedMemberLevel.getCurrentPoint() != null ? linkedMemberLevel.getCurrentPoint() : BigDecimal.ZERO;
            BigDecimal totalPoints = mainPoints.add(linkedPoints);
            
            Long mainOrders = mainMemberLevel.getTotalOrders() != null ? mainMemberLevel.getTotalOrders() : 0L;
            Long linkedOrders = linkedMemberLevel.getTotalOrders() != null ? linkedMemberLevel.getTotalOrders() : 0L;
            Long totalOrders = mainOrders + linkedOrders;
            
            // 3. 根据总积分计算新的会员等级
            Integer newLevel = calculateMemberLevel(totalPoints);
            
            // 4. 更新主账号等级
            mainMemberLevel.setCurrentPoint(totalPoints);
            mainMemberLevel.setCurrentLevel(newLevel);
            mainMemberLevel.setTotalOrders(totalOrders);
            memberLevelService.updateMemberLevel(mainMemberLevel);
            
            // 5. 更新关联账号等级（同步）
            linkedMemberLevel.setCurrentPoint(totalPoints);
            linkedMemberLevel.setCurrentLevel(newLevel);
            linkedMemberLevel.setTotalOrders(totalOrders);
            memberLevelService.updateMemberLevel(linkedMemberLevel);
            
            // 6. 设置双向关联
            Map<String, Object> params1 = new HashMap<>();
            params1.put("memberId", memberId);
            params1.put("linkedAccountId", linkedAccountId);
            params1.put("updateBy", "admin");
            
            Map<String, Object> params2 = new HashMap<>();
            params2.put("memberId", linkedAccountId);
            params2.put("linkedAccountId", memberId);
            params2.put("updateBy", "admin");
            
            memberMapper.updateLinkedAccount(params1);
            memberMapper.updateLinkedAccount(params2);
            
            // 7. 为每个账号添加积分历史记录（不更新会员等级）
            addMergePointHistory(memberId, linkedAccountId, mainPoints, linkedPoints, totalPoints);
            
            // 8. 记录合并历史
            recordMergeHistory(memberId, linkedAccountId, mainPoints, linkedPoints, totalPoints, 
                              mainOrders, linkedOrders, totalOrders, newLevel);
            
            result.put("success", true);
            result.put("message", "Accounts merged successfully");
            result.put("totalPoints", totalPoints);
            result.put("totalOrders", totalOrders);
            result.put("newLevel", newLevel);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to merge accounts: " + e.getMessage());
            log.error("Account merge failed for memberId: {} and linkedAccountId: {}", memberId, linkedAccountId, e);
            throw new RuntimeException("Account merge failed", e);
        }
        
        return result;
    }

    /**
     * 为账号合并添加积分历史记录（不更新会员等级）
     */
    private void addMergePointHistory(Long memberId, Long linkedAccountId, 
                                     BigDecimal mainPoints, BigDecimal linkedPoints, BigDecimal totalPoints) {
        try {
            // 为主账号添加积分历史记录（只插入记录，不更新等级）
            BigDecimal mainAmount = linkedPoints.multiply(new BigDecimal("100"));
            String mainNote = String.format("Account merge: Combined with account %d. Original points: %s, Merged points: %s", 
                                           linkedAccountId, mainPoints, totalPoints);
            insertPointHistoryOnly(memberId, mainAmount, mainNote, 0);
            
            // 为关联账号添加积分历史记录（只插入记录，不更新等级）
            BigDecimal linkedAmount = mainPoints.multiply(new BigDecimal("100"));
            String linkedNote = String.format("Account merge: Combined with account %d. Original points: %s, Merged points: %s", 
                                             memberId, linkedPoints, totalPoints);
            insertPointHistoryOnly(linkedAccountId, linkedAmount, linkedNote, 0);
            
            log.info("Added merge point history for both accounts - Main: {}, Linked: {}", memberId, linkedAccountId);
        } catch (Exception e) {
            log.error("Failed to add merge point history for memberId: {} and linkedAccountId: {}", memberId, linkedAccountId, e);
            // 不抛出异常，避免影响合并流程
        }
    }

    /**
     * 只插入积分历史记录，不更新会员等级
     */
    private void insertPointHistoryOnly(Long memberId, BigDecimal amount, String note, Integer platform) {
        MemberPointHistory pointHistory = new MemberPointHistory();
        pointHistory.setMemberId(memberId);
        pointHistory.setAmount(amount);
        BigDecimal points = amount.multiply(new BigDecimal("0.01")).setScale(4, java.math.RoundingMode.HALF_UP);
        pointHistory.setPoints(points);
        pointHistory.setNote(note);
        pointHistory.setPlatform(platform);
        memberPointHistoryMapper.insertPointHistory(pointHistory);
    }

    /**
     * 记录账号合并历史
     */
    private void recordMergeHistory(Long memberId, Long linkedAccountId, 
                                   BigDecimal mainPoints, BigDecimal linkedPoints, BigDecimal totalPoints,
                                   Long mainOrders, Long linkedOrders, Long totalOrders, Integer newLevel) {
        // 这里可以添加合并历史记录的逻辑
        // 比如记录到专门的合并历史表中
        log.info("Account merge completed - Main: {}, Linked: {}, Total Points: {}, Total Orders: {}, New Level: {}", 
                 memberId, linkedAccountId, totalPoints, totalOrders, newLevel);
    }

    /**
     * 根据积分计算应该的会员等级
     */
    private Integer calculateMemberLevel(BigDecimal totalPoints) {
        // 查询所有会员等级，按积分要求升序排列
        List<com.medusa.mall.domain.member.MemberBenefit> memberBenefits = memberBenefitMapper.selectAllMemberBenefitsOrderByPoint();
        
        Integer targetLevel = 1; // 默认等级为1
        
        // 遍历所有等级，找到积分要求最高的且不超过当前积分的等级
        for (com.medusa.mall.domain.member.MemberBenefit benefit : memberBenefits) {
            if (benefit.getPoint() != null && totalPoints.compareTo(benefit.getPoint()) >= 0) {
                targetLevel = benefit.getLevelId().intValue();
            } else {
                // 如果当前等级的积分要求超过了总积分，停止遍历
                break;
            }
        }
        
        return targetLevel;
    }

    /**
     * 查询所有活跃会员
     * 
     * @return 活跃会员列表
     */
    @Override
    public List<Member> selectActiveMembers() {
        return memberMapper.selectActiveMembers();
    }
}
