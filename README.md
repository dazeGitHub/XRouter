# XRouter

支持 kotlin 的注解处理器的路由框架

##### 1. build.gradle 添加依赖

```groovy
implementation 'com.github.dazeGitHub:XRouter:5.0.5'
annotationProcessor 'com.github.dazeGitHub.XRouter:annotation_compiler:5.0.5'
kapt 'com.github.dazeGitHub.XRouter:annotation_compiler:5.0.5'
```

##### 2. 使用路由

```java
@Route(key = Constants.RouterPath.MAIN)
class MainActivity : BaseActivity() {
    fun jumpLoginActivity(view: View?) {
//        liveData.postValue("testData2_postValue");
//        startActivity(Intent(this, XRouter.getInstance().getActivity("login/login")))
        XRouter.getInstance().jumpActivity(this,  url = Constants.RouterPath.LOGIN_TEST_PATH,scheme = null)
    }
}
```

字段自动注入

```java
@Route(key = Constants.RouterPath.LOGIN)
class LoginActivity : BaseActivity() {

    @Autowired("age")
    var age: Int? = null
    @Autowired(value = "username")
    var userNameStr: String? = null
    @Autowired(value = "user")
    var userObj: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_login)
      XRouterKnife.bind(this, IJsonTransferImp())

      "LoginActivity age = $age userNameStr = $userNameStr userObj = $userObj".let{
          TLog.e(TAG,it)
          findViewById<TextView>(R.id.tv_receive_msg).text = it
    }  
}
```

```java
class IJsonTransferImp : IJsonTransfer {
    override fun transJson2Obj(json: String?, clazz: Class<*>): Any? {
        return when(clazz){
            User::class.java -> {
                Constants.GSON.fromJson<User>(json, clazz)
            }
            else -> {
                null
            }
        }
    }
}
```
