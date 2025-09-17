package co.com.claro.hogarconectado

import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.thingclips.smart.api.MicroContext
import com.thingclips.smart.api.router.UrlBuilder
import com.thingclips.smart.api.service.RedirectService
import com.thingclips.smart.api.service.RouteEventListener
import com.thingclips.smart.api.service.ServiceEventListener
import com.thingclips.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk
import com.thingclips.smart.theme.ThingThemeInitializer.init
import com.thingclips.smart.wrapper.api.ThingWrapper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Please don't change the order.
        // 请不要修改初始化顺序
        Fresco.initialize(this)

        ThingHomeSdk.init(this)

        ThingWrapper.init(this, object : RouteEventListener {
            override fun onFaild(errorCode: Int, urlBuilder: UrlBuilder) {
                // urlBuilder.target is a router address, urlBuilder.params is a router params
                //点击无反应表示路由未现实，需要在此实现， urlBuilder.target 目标路由， urlBuilder.params 路由参数
                Log.e(
                    "router not implement",
                    urlBuilder.target + " : " + urlBuilder.params.toString()
                )
            }
        }, object : ServiceEventListener {
            override fun onFaild(serviceName: String) {
                Log.e("service not implement", serviceName)
            }
        })
        init(this)
        ThingOptimusSdk.init(this)


        // register family service，mall bizbundle don't have to implement it.
        // 注册家庭服务，商城业务包可以不注册此服务
        ThingWrapper.registerService<AbsBizBundleFamilyService?, AbsBizBundleFamilyService?>(
            AbsBizBundleFamilyService::class.java,
            BizBundleFamilyServiceImpl()
        )


        //Intercept existing routes and jump to custom implementation pages with parameters
        //拦截已存在的路由，通过参数跳转至自定义实现页面
        val service = MicroContext.getServiceManager()
            .findServiceByInterface<RedirectService?>(RedirectService::class.java.getName())
        service.registerUrlInterceptor(object : RedirectService.UrlInterceptor {
            override fun forUrlBuilder(
                urlBuilder: UrlBuilder?,
                interceptorCallback: RedirectService.InterceptorCallback,
            ) {
                //Such as:
                //Intercept the event of clicking the panel right menu and jump to the custom page with the parameters of urlBuilder
                //例如：拦截点击面板右上角按钮事件，通过 urlBuilder 的参数跳转至自定义页面
                // if (urlBuilder.target.equals("panelAction") && urlBuilder.params.getString("action").equals("gotoPanelMore")) {
                //     interceptorCallback.interceptor("interceptor");
                //     Log.e("interceptor", urlBuilder.params.toString());
                // } else {
                interceptorCallback.onContinue(urlBuilder)
                // }
            }
        })

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}