package com.example.e_commerce_payment.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_commerce_payment.R
import com.example.e_commerce_payment.Validator
import com.example.e_commerce_payment.api.ApiConfig
import com.example.e_commerce_payment.api.ApiService
import com.example.e_commerce_payment.databinding.ActivityLoginBinding
import com.example.e_commerce_payment.models.MessageLoginResponse
import com.example.e_commerce_payment.storage.MyPreferenceManager
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogAnimation
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.aestheticdialogs.OnDialogClickListener
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), View.OnClickListener, Validator {
    private lateinit var binding: ActivityLoginBinding
    private var doubleBackToExitPressedOnce = false
    private lateinit var mail: String
    private lateinit var pass: String

    var preferenceManager: MyPreferenceManager? = null
    private var saveLogin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addEvents()
        preferenceManager = MyPreferenceManager(this)

    }

    private fun addEvents() {
        binding.btnBack.setOnClickListener(this)
        binding.btnLoginFb.setOnClickListener(this)
        binding.btnLoginGg.setOnClickListener(this)
        binding.btnGotoSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.btnGotoForgot.setOnClickListener(this)

        if (preferenceManager?.getIsLogin() == true) {
            binding.edtEmail.setText(preferenceManager!!.getEmail())
            binding.edtPassword.setText(preferenceManager!!.getPassword())
            binding.cbxRemember.isChecked = true
        } else {
            binding.edtEmail.setText("")
            binding.edtPassword.setText("")
            binding.cbxRemember.isChecked = false
        }
    }

    private fun gotoSignUp() {
        intent = intent.setClass(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun gotoForgotPassword() {
        intent = intent.setClass(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun login() {
        mail = binding.edtEmail.text.toString().trim()
        pass = binding.edtPassword.text.toString().trim()

        if (validateEmail(mail) && validatePassword(pass)) {

            bindProgressButton(binding.btnLogin)
            binding.btnLogin.attachTextChangeAnimator()
            binding.btnLogin.showProgress {
                buttonTextRes = R.string.loading_text
                progressColor = getColor(R.color.WHITE)
            }

            if (binding.cbxRemember.isChecked) {
                Log.d("LoginActivity", "Saved email, pass in SharePref: $mail $pass")
                saveLogin = true
                preferenceManager?.saveLogin(this, mail, pass, true)
            } else {
                Log.d("LoginActivity", "Not saved email, pass in SharePref")
                saveLogin = false
                preferenceManager?.saveLogin(this, "", "", false)
            }

            val apiService: ApiService = ApiConfig.setUpRetrofit().create(ApiService::class.java)
            val call = apiService.login(mail, pass)
            Log.d("LoginActivity", "call api login: $mail, $pass")

            call.enqueue(object : Callback<MessageLoginResponse> {
                override fun onResponse(
                    call: Call<MessageLoginResponse>,
                    response: Response<MessageLoginResponse>
                ) {

                    if (response.isSuccessful) {
                        val msg: String = response.body()?.msg.toString()
                        if (msg == "Please check email to enter code") {
                            Log.d("LoginActivity", "Login done! goto OTP")
                            Toast.makeText(
                                this@LoginActivity,
                                "Check your mail to get OTP",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnLogin.hideProgress("Goto OTP")

                            intent = Intent(this@LoginActivity, OtpActivity::class.java)
                            intent.putExtra("email", mail)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val jsonObject = errorBody?.let { JSONObject(it) }
                            val errorMessage = jsonObject?.getString("message")

                            Log.e("LoginActivity", "Login fail: $errorMessage")

                            // Xử lý lỗi dựa trên thông báo lỗi nhận được
                            // ...
                            Log.e("LoginActivity", "login sai: " +errorMessage)
                            binding.btnLogin.hideProgress("LOGIN AGAIN!")

                            AestheticDialog.Builder(
                                this@LoginActivity,
                                DialogStyle.TOASTER,
                                DialogType.ERROR
                            )
                                .setTitle("Error")
                                .setMessage("Email or password is incorrect! Check it again")
                                .setCancelable(false)
                                .setDarkMode(false)
                                .setGravity(Gravity.TOP)
                                .setAnimation(DialogAnimation.SLIDE_DOWN)
                                .setOnClickListener(object : OnDialogClickListener {
                                    override fun onClick(dialog: AestheticDialog.Builder) {
                                        dialog.dismiss()
                                        binding.edtEmail.requestFocus()
                                    }
                                })
                                .show()

                        } catch (e: JSONException) {
                            Log.e("LoginActivity", "Error parsing error response: ${e.message}")

                            // Xử lý lỗi khi không thể phân tích thông báo lỗi
                            // ...
                        }



                    }
                }

                override fun onFailure(call: Call<MessageLoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login fail, check your network",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("LoginActivity", "Login fail, check your network")
                    binding.btnLogin.hideProgress("LOGIN AGAIN!")
                }
            })

        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnBack.id -> {
                intent = intent.setClass(this, AppIntro::class.java)
                startActivity(intent)
            }

            binding.btnLoginFb.id -> {
                Toast.makeText(this, "Login with Fb coming soon", Toast.LENGTH_SHORT).show()
            }

            binding.btnLoginGg.id -> {
                Toast.makeText(this, "Login with Gg coming soon", Toast.LENGTH_SHORT).show()
            }

            binding.btnGotoForgot.id -> {
                gotoForgotPassword()
            }

            binding.btnLogin.id -> {
                login()
            }

            binding.btnGotoSignUp.id -> {
                gotoSignUp()
            }
        }
    }

    override fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = ""
            binding.layoutEmail.setEndIconDrawable(R.drawable.ic_tick)
            true
        } else if (email.isEmpty()) {
            binding.layoutEmail.error = "Email can't be empty"
            binding.edtEmail.requestFocus()
            false
        } else if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Not a valid email address. Should be your@email.com"
            binding.edtEmail.requestFocus()
            false
        } else {
            binding.layoutEmail.error = "Something went wrong"
            binding.edtEmail.requestFocus()
            false
        }
    }

    override fun validatePassword(password: String): Boolean {
        if (password.isNotEmpty() && password.length >= 6) {
            binding.layoutPassword.error = ""
            return true
        } else if (password.isEmpty()) {
            binding.layoutPassword.error = "Password can't be empty"
            binding.edtPassword.requestFocus()
            return false
        } else if (password.isNotEmpty() && password.length < 6) {
            binding.layoutPassword.error = "Password must be at least 6 characters"
            binding.edtPassword.requestFocus()
            return false
        } else {
            binding.layoutPassword.error = "Something went wrong"
            binding.edtPassword.requestFocus()
            return false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

}