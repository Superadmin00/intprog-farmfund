package com.intprog.farmfund.fragments

import android.content.Context
import android.content.Intent
import android.content.DialogInterface
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.intprog.farmfund.activities.ForgotPasswordActivity
import com.intprog.farmfund.activities.NavigatorActivity
import com.intprog.farmfund.databinding.BottomsheetLoginBinding
import com.intprog.farmfund.objects.LoadingDialog

class LoginBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomsheetLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var gso: GoogleSignInOptions

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("561319474391-1dhmej9q43ftotfgf0gdaur82k2iikbr.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.google.setOnClickListener {
            signInWithGoogle()
        }

        binding.toRegisterLayout.setOnClickListener {
            dismiss()
        }

        binding.toGuestLayout.setOnClickListener {
            val intent = Intent(activity, NavigatorActivity::class.java)
            startActivity(intent)
        }

        binding.forgot.setOnClickListener {
            val intent = Intent(activity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Get the email and password from the intent arguments
        val emailNum = arguments?.getString("email")
        val passW = arguments?.getString("password")

        // Set the text of the TextInputEditTexts to the email and password
        binding.emailNumEditText.setText(emailNum)
        binding.passwordEditText.setText(passW)

        var user: FirebaseUser?
        binding.loginBTN.setOnClickListener {
            val emailOrNumber = binding.emailNumEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            val validEmailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val validPhonePattern = "^09[0-9]{9}$"

            var email: String? = null
            var number: String? = null

            if (emailOrNumber.isNotEmpty()) {
                if (emailOrNumber.matches(validEmailPattern.toRegex())) {
                    email = emailOrNumber
                } else if (emailOrNumber.matches(validPhonePattern.toRegex())) {
                    number = emailOrNumber
                } else {
                    binding.emailNumberInputLayout.error = "This is not a valid input."
                    return@setOnClickListener
                }
            } else {
                binding.emailNumberInputLayout.error = "This field is required!"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordTextInputLayout.error = "This field is required!"
                return@setOnClickListener
            } else {
                binding.emailNumberInputLayout.error = null
            }

            // Show loading dialog
            LoadingDialog.show(requireContext(), false)

            auth.signInWithEmailAndPassword(emailOrNumber, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Close Loading dialog
                        LoadingDialog.dismiss()
                        user = auth.currentUser
                        binding.emailNumberInputLayout.error = null
                        binding.passwordTextInputLayout.error = null
                        Toast.makeText(context, "Login Successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, NavigatorActivity::class.java)
                        startActivity(intent)
                    } else {
                        LoadingDialog.dismiss()
                        binding.emailNumberInputLayout.error = "Incorrect Login Credentials"
                        binding.passwordTextInputLayout.error = "Incorrect Login Credentials"
                        Toast.makeText(context, "Authentication Error. Unable to Proceed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Code to adjust the screen when inputting/typing
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rootView = view.rootView
                val diff = rootView.height - view.height
                if (diff > dpToPx(200)) { // If the difference is more than 200dp, the keyboard is probably shown
                    // Scroll up the bottom sheet
                    (dialog as BottomSheetDialog).behavior.setPeekHeight(view.height)
                } else {
                    // Reset the position of the bottom sheet when the keyboard is hidden
                    (dialog as BottomSheetDialog).behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO)
                }
            }

            private fun dpToPx(dp: Int): Int {
                return (dp * Resources.getSystem().displayMetrics.density).toInt()
            }
        })

        // Code to close keyboard when clicking outside the input fields
        binding.mainLayout.setOnClickListener {
            val inputMethodManager =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        addTextWatcher(binding.emailNumEditText, binding.emailNumberInputLayout)
        addTextWatcher(binding.passwordEditText, binding.passwordTextInputLayout)
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    Toast.makeText(context, "This Google account is not registered.", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.delete()
                } else {
                    val user = auth.currentUser
                    Toast.makeText(context, "Google Sign-In Successful.", Toast.LENGTH_SHORT).show()
                    dismiss()
                    val intent = Intent(context, NavigatorActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Authentication Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addTextWatcher(editText: TextInputEditText, layout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                layout.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val registerBottomSheetDialogFragment = RegisterBottomSheetDialogFragment()
        dismiss()
        registerBottomSheetDialogFragment.show(
            parentFragmentManager,
            registerBottomSheetDialogFragment.tag
        )
    }
}
