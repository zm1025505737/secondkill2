<%--
  Created by IntelliJ IDEA.
  User: adminitrator
  Date: 2020/5/28
  Time: 14:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>秒杀页面</title>
</head>
<body>
    <form class="form1" action="sk/doSK" method="post">
        <input type="hidden" name="id" value="10001">
        <input type="button" id="miaosha_btn" name="seckill_btn" value="秒杀点我" />
    </form>

    <hr>
    <hr>

    <form>
        <input type="text" name="phone"/>
        <input type="button" value="发送验证码"/>
        <br/>
        <input type="text" name="code"/>
        <input type="button" value="确定"/>

    </form>


</body>
<script type="text/javascript" src="jquery/jquery-2.1.1.min.js"></script>
<script type="text/javascript">


    $(function () {

        $("#miaosha_btn").click(function () {
            $.ajax({
                url:$(".form1").prop("action"),
                data:$(".form1").serialize(),
                type:"post",
                success:function (data) {
                    if (data=="ok"){
                        alert("秒杀成功!");
                    }else {
                        alert(data);
                        $("#miaosha_btn").prop("disabled",true);
                    }
                }
            })
            return false;
        })

    })
</script>

</html>
