package com.jimei.mybatis2;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.youpin.common.base.InDTO;
import com.youpin.common.base.OutDTO;
import com.youpin.common.utils.FormatUtil;
import com.youpin.user.dao.entity.BuyerInfo;
import com.youpin.user.dao.mapper.BuyerInfoMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yudm
 * @date 2020/2/24 12:32
 * @description
 */
@Api(tags = "环境搭建测试")
@RequestMapping()
@RestController
@Slf4j
public class HelloWorld {
    @Resource
    private SqlExecutor sqlExecutor;

    /*@Resource
    private FuncResolverProxy funcResolverProxy;*/
    @Resource
    private BuyerInfoMapper buyerInfoMapper;

    @PostMapping("/update")
    public Result update(Cause cause) {
        return Result.success(sqlExecutor.executeUpdate(cause.getLabel(), cause.getSql(), cause.getParams()));
    }

    @PostMapping("/query")
    public Result query(Cause cause) {
        if (true) {
            throw new RuntimeException();
        }
        return Result.success(sqlExecutor.executeQuery(cause.getLabel(), cause.getSql(), cause.getParams()));
    }

    @ApiOperation(tags = "环境搭建测试", value = "hello world 接口")
    //@Url(label="xxx")如果只有一个通用接口则这里的value不用写，FuncInfo中的urlLabel也不用写值
    @PostMapping("/hello")
    public OutDTO hello(@RequestBody @Valid InDTO inDTO) {

        return OutDTO.success(inDTO);
    }

    @ApiOperation(tags = "环境搭建测试", value = "Mybatis增强测试")
    @PostMapping("/insertTest")
    public OutDTO insertTest(@RequestBody @Valid InDTO<BuyerInfo> inDTO) {
        Table.insert(inDTO.getReq());

        return OutDTO.success(inDTO);
    }

    @GetMapping("/hello1")
    public String hello1() {
        String str1 = "yu_ding_ming";
        String str3 = "YU_DING_MING";
        String str2 = "yu-ding-ming";
        String str4 = "DING";
        String str5 = "DING5";
        return FormatUtil.underToLower(str4) + "---" + FormatUtil.underToLower(str5);
    }

    /*@ApiOperation(tags = "环境搭建测试", value = "hello world 接口")
    //@Url(label="xxx")如果只有一个通用接口则这里的value不用写，FuncInfo中的urlLabel也不用写值
    @PostMapping("/hello2")
    public OutDTO hello2(@RequestBody InDTOTest inDTO) {
        //Map<String, Object> map = BeanUtil.beanToMap(in.getBusiInfo());
        *//*Map<String,?> map = new HashMap<>();
        map.put("1", "x");
        String str1 = map.get("1");*//*
     *//*TestMap<String, Object> map = new TestMap<>();
        map.put(0, "x");
        map.put(1, 3);
        map.put(2, 3L);
        String str = map.get(0);
        String str2 = map.get(1);*//*
     *//*Map<String, Object> map = new HashMap<>();
        map.put("1", "x");
        map.put("2", 20);
        map.put("3", 0.5f);
        Object str = MapUtil.getRtmVal(map.get("1").getClass());
        Long rs = DynamicMapper.selectOne(null, null, null);*//*
        return OutDTO.success(funcResolverProxy.resolve(inDTO.getFuncCode(), inDTO.getInData(), inDTO.getHead()));
    }*/
    @PostMapping("/test")
    public String test() {
        List<String> list = new ArrayList<>();
        list.add("keyId");
        list.add("userId");
        list.add("userPwd");
        list.add("userName");
        list.add("phoneNum");
        list.add("userIconUrl");
        list.add("uerLevel");
        list.add("status");
        list.add("createDate");
        list.add("opDate");
        Class clazz = BuyerInfo.class;
        Long startTime1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            Field[] fields = clazz.getDeclaredFields();
            Map<String, Field> map = new HashMap<>();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    map.put(field.getName(), field);
                }
            }
            for (String str : list) {
                map.get(str);
            }
        }
        Long endTime1 = System.currentTimeMillis();
        Long startTime2 = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            for (String str : list) {
                try {
                    clazz.getDeclaredField(str);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        Long endTime2 = System.currentTimeMillis();
        return (startTime1 - endTime1) + "===" + (startTime2 - endTime2);
    }

    @PostMapping("test3")
    public Object test3() {
        DruidDataSource ds = new DruidDataSource();
        ds.getMaxWaitThreadCount();
        return DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
    }
}











