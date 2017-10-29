package com.example.leeyun.stringting_android;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.example.leeyun.stringting_android.R.id.Provision_Linkify;
import static com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q;


public class MainActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private Button CustomloginButton;
    private CallbackManager callbackManager;

    static String Email;
    GlobalApplication globalApplication=(GlobalApplication)getApplication();

    //rest api를 위한 변수선언
    Retrofit retrofit;
    Rest_ApiService apiService;

    class Strings extends Application {

        private String Facebook_email;

        public String getState(){
            return Facebook_email;
        }
        public void setState(String s){
            Facebook_email = s;
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // SDK 초기화 (setContentView 보다 먼저 실행되어야합니다. 안그럼 에러납니다.)
        setContentView(R.layout.activity_main);

        retrofit = new Retrofit.Builder().baseUrl(Rest_ApiService.API_URL).build();
        apiService=retrofit.create(Rest_ApiService.class);


        //Restrofit_test
        Call<ResponseBody>comment =apiService.getComment(1);
        comment.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call <ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Log.v("Test",response.body().string());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });



        TextView Provision_Linkify =(TextView)findViewById(R.id.Provision_Linkify);

        String text="가입하기 또는 로그인 버튼을 누루면 이용약관 및 개인정보취급방침에 동의하신 것이 됩니다.";
                Provision_Linkify.setText(text);

        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };

        Pattern pattern1 = Pattern.compile("이용약관");
        Pattern pattern2 = Pattern.compile("개인정보취급방침");

        Linkify.addLinks(Provision_Linkify, pattern1, "http://www.naver.com",null,mTransform);
        Linkify.addLinks(Provision_Linkify, pattern2, "http://gun0912.tistory.com/",null,mTransform);

        //이용약관 및 개인정보 취급방식에대한 링크



        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //로그아웃 성공 후 하고싶은 내용 코딩 ~
            }
        });

        SessionCallback callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        
        callbackManager = CallbackManager.Factory.create();  //로그인 응답을 처리할 콜백 관리자
        loginButton = (LoginButton) findViewById(R.id.buttonId); //페이스북 로그인 버튼
        //유저 정보, 친구정보, 이메일 정보등을 수집하기 위해서는 허가(퍼미션)를 받아야 합니다.
        loginButton.setReadPermissions("public_profile", "user_friends", "email");
        //버튼에 바로 콜백을 등록하는 경우 LoginManager에 콜백을 등록하지 않아도됩니다.
        //반면에 커스텀으로 만든 버튼을 사용할 경우 아래보면 CustomloginButton OnClickListener안에 LoginManager를 이용해서
        //로그인 처리를 해주어야 합니다.

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) { //로그인 성공시 호출되는 메소드
                Log.e("토큰", loginResult.getAccessToken().getToken());
                Log.e("유저아이디", loginResult.getAccessToken().getUserId());
                Log.e("퍼미션 리스트", loginResult.getAccessToken().getPermissions() + "");

                //loginResult.getAccessToken() 정보를 가지고 유저 정보를 가져올수 있습니다.
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.e("user profile", object.toString());
                                    Email   = response.getJSONObject().getString("id").toString();
                                    Log.e("email:",Email);
                                    Intent intent = new Intent(MainActivity.this,  Basicinfo_Edit.class);
                                    intent.putExtra("facebook_email",Email);
                                    intent.putExtra("setid",'F');
                                    startActivity(intent);


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onError(FacebookException error) {
            }

            @Override
            public void onCancel() {
            }

            
           
            
        });

        Call<ResponseBody>comment2 =apiService.getPostCommentStr("test");
        comment2.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call <ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Log.v("Test",response.body().string());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });


        CustomloginButton = (Button)findViewById(R.id.loginBtn);
        CustomloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
//                LoginManager - 요청된 읽기 또는 게시 권한으로 로그인 절차를 시작합니다.
//                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,
//                        Arrays.asList("public_profile", "user_friends"));
//                LoginManager.getInstance().registerCallback(callbackManager,
//                        new FacebookCallback<LoginResult>() {
//                            @Override
//                            public void onSuccess(LoginResult loginResult) {
//                                Log.e("onSuccess", "onSuccess");
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                Log.e("onCancel", "onCancel");
//                            }
//
//                            @Override
//                            public void onError(FacebookException exception) {
//                                Log.e("onError", "onError " + exception.getLocalizedMessage());
//                            }
//                        });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void onClick_membership(View v){
        Intent intent = new Intent(getApplicationContext(),Membership_form.class);

        startActivity(intent);

    }
    public void onClick_login_firstpage(View v){
        Intent exintent = new Intent(getApplicationContext(),Login_firstpage.class);

        startActivity(exintent);

    }

    

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    Log.e("UserProfile", userProfile.toString());
                    String kakaoID = String.valueOf(userProfile.getId()); // userProfile에서 ID값을 가져옴
                    String kakaoNickname = userProfile.getNickname();     // Nickname 값을 가져옴
                    Log.e("KakaoId", kakaoID);

                    Intent intent = new Intent(MainActivity.this,  Basicinfo_Edit.class);
                    intent.putExtra("kakaoID",kakaoID);
                    intent.putExtra("setid",'K');
                    startActivity(intent);
                }
            });

        }


        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // 세션 연결이 실패했을때
            // 어쩔때 실패되는지는 테스트를 안해보았음 ㅜㅜ
        }
    }

    public interface Rest_ApiService {

        public  static  final String API_URL="http://jsonplaceholder.typicode.com/";

        @GET("comments")
        Call<okhttp3.ResponseBody> getComment(@Query("postId")int testid);

        @FormUrlEncoded
        @POST("comments")
        Call<ResponseBody>getPostCommentStr(@Field("postId")String testid);
    }

}


