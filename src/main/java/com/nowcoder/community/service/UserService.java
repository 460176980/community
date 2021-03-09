package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Service
public class UserService implements CommunityConstant {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine; //模板引擎,创建实例开销大，建议只实例化一个

    @Value("${community.path.domain}")
    private String domain;                 //从application属性中获取域名配置

    @Value("${server.servlet.context-path}")
    private String contextPath;             //什么上下文？？

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //注册请求时检查用户(因为注册时需要查看用户是否已经被注册或者信息是否合法)
    public Map<String,Object> register(User user){
        Map<String,Object> map =new HashMap<>();
        //1.查询用户传入的参数是否正确
        if(user==null){
            throw  new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","，密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","，密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","，密码不能为空");
            return map;
        }

        //2.然后才去数据库查看信息
        User u=userMapper.selectByName(user.getUsername());
        //2.1 如果查到了用户，则返回消息
        if(u!=null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //3.没有异常后再设置信息，并存入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));  //Salt相当于是"鸡肋"信息，给黑客造成干扰
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        //user.setActivationCode("123");
        user.setActivationCode(CommunityUtil.generateUUID()); //随机生成激活码
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000))); //随机生成头像(网上有链接)
        user.setCreateTime(new Date());
        //3.1 插入数据库
        //3.2 由于application中设置了mybatis.configuration.useGeneratedKeys=true，所以会自动生成id，无需手动设置
        userMapper.insertUser(user);

        //4.激活邮件
        //4.1设置Thymeleaf上下文容器，设置key-value变量改变网页模板
        Context context=new Context();
        context.setVariable("email",user.getEmail());

        // 如：http://local:8080/community/activation/101/code
        //4.2域名+servlet上下文+
        String url=domain+contextPath+"/activation"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);

        //4.3 将context实例传给名称为"/mail/activation"的模板
        String content =templateEngine.process("mail/activation",context);
        //System.out.println("发送内容:url+content"+url+content);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //5.最终返回空的map，没有错误信息
        return map;
    }

    public int activation(int userId, String code) {
        //通过id查询用户
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

}
