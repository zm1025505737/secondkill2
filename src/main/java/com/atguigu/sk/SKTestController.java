package com.atguigu.sk;

import com.atguigu.sk.utils.JedisPoolUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

@RestController
public class SKTestController {
    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + "   return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + "   return 0;\r\n"
            + "else \r\n"
            + "   redis.call(\"decr\",qtkey);\r\n"
            + "   redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";
//    使用LUA脚本  实现秒杀,解决高并发乐观锁库存剩余问题,
    @PostMapping(value = "/sk/doSK",produces = "text/html;charset=UTF-8;")
    public String doSK(Integer id){
        Integer uerId = (int)(Math.random()*10000);
       //使用jedis连接池
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        //加载脚本
        String sha = jedis.scriptLoad(secKillScript);
        //传入参数用户id 和商品id
        Object evalsha = jedis.evalsha(sha, 2, uerId+"", id+"");

        int i = (int)((long)evalsha);
        if (i == 1){
            System.out.println("秒杀成功"+ uerId);
            jedis.close();
            return "ok";
        }else if(i == 2){
            System.out.println("重复秒杀"+ uerId);
            jedis.close();
            return "重复秒杀";
        }else {
            System.out.println("库存不足" + uerId);
            jedis.close();
            return "库存不足";
        }


    }





    //使用乐观锁
//    @PostMapping(value = "/sk/doSK",produces = "text/html;charset=UTF-8;")
    public String doSK2(Integer id){
        Integer uerId = (int)(Math.random()*10000);
        String pidKey = "sk:"+ id + ":qt";
        String userKey = "sk:"+ id +":usr";

        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        if (jedis.sismember(userKey, uerId + "")){
            System.out.println("重复秒杀 "+uerId);
            return "重复秒杀";
        }
        //监视开始
        jedis.watch(pidKey);


        String pidVal = jedis.get(pidKey);
        System.out.println("库存= :" + pidVal);
        if (pidVal == null){
            System.out.println("活动尚未开始 "+uerId);
            return "活动尚未开始";
        }
        int pid = Integer.valueOf(pidVal);
        if (pid <= 0){
            System.out.println("库存不足 "+ uerId);
            jedis.close();
            return "库存不足";
        }
        //============multi组队开始============
        Transaction multi = jedis.multi();
        //秒杀后数量 - 1
        multi.decr(pidKey);

        multi.sadd(userKey,uerId+"");
        //批量执行
        multi.exec();
        //关闭连接
        jedis.close();
        System.out.println("秒杀成功 " +uerId);

        return "ok";
    }


    //不使用乐观锁,会导致负数的发生
//    @PostMapping(value = "/sk/doSK",produces = "text/html;charset=UTF-8;")
    public String doSK1(Integer id){
        Integer uerId = (int)(Math.random()*10000);
        String pidKey = "sk:"+ id + ":qt";
        String userKey = "sk:"+ id +":usr";

//        Jedis jedis1 = new Jedis("192.168.0.107", 6379);
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        if (jedis.sismember(userKey, uerId + "")){
            System.out.println("重复秒杀 "+uerId);
            jedis.close();
            return "重复秒杀";
        }

        String pidVal = jedis.get(pidKey);
//        System.out.println("库存 :" + pidVal);
        if (pidVal == null){
            System.out.println("活动尚未开始 "+uerId);
            return "活动尚未开始";
        }
        int pid = Integer.valueOf(pidVal);
        if (pid <= 0){
            System.out.println("库存不足 "+ uerId);
            return "库存不足";
        }

        jedis.decr(pidKey);

        jedis.sadd(userKey,uerId+"");
        jedis.close();
        System.out.println("秒杀成功 " +uerId);

        return "ok";
    }
}
