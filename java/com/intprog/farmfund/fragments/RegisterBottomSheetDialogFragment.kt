package com.intprog.farmfund.fragments

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import com.intprog.farmfund.databinding.BottomsheetRegisterBinding
import com.intprog.farmfund.dataclasses.User
import com.intprog.farmfund.objects.HideKeyboardOnClick
import com.intprog.farmfund.objects.LoadingDialog

class RegisterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomsheetRegisterBinding? = null
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
        _binding = BottomsheetRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("561319474391-1dhmej9q43ftotfgf0gdaur82k2iikbr.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.toLogin.setOnClickListener {
            dismiss()
        }
        binding.google.setOnClickListener {
            signInWithGoogle()
        }

        binding.registerButton.setOnClickListener {
            val email = binding.enterEmailNum.text.toString()
            val name = binding.enterName.text.toString()
            val password = binding.enterPassword.text.toString()
            val confirmPassword = binding.enterConfirmPassword.text.toString()
            var hasError = false

            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val bundle = Bundle()

            if (email.isEmpty()) {
                binding.enterEmailNumLayout.error = "This field is required!"
                hasError = true
            } else if (!email.matches(emailPattern.toRegex())) {
                binding.enterEmailNumLayout.error = "Invalid email format."
                hasError = true
            } else {
                binding.enterEmailNumLayout.error = null
            }

            if (name.isEmpty()) {
                binding.enterNameLayout.error = "This field is required!"
                hasError = true
            } else{
                binding.enterNameLayout.error = null
            }

            if (password.isEmpty()) {
                binding.enterPasswordLayout.error = "This field is required!"
                hasError = true
            }else{
                binding.enterPasswordLayout.error = null
            }

            if (confirmPassword.isEmpty()) {
                binding.enterConfirmPasswordLayout.error = "This field is required!"
                hasError = true
            }else{
                binding.enterConfirmPasswordLayout.error = null
            }

            if (password != confirmPassword) {
                binding.enterPasswordLayout.error = "Passwords don't match."
                binding.enterConfirmPasswordLayout.error = "Passwords don't match."
                hasError = true
            } else {
                if (!password.isEmpty()) {
                    binding.enterPasswordLayout.error = null // Clear error if it's valid
                }
                if (!confirmPassword.isEmpty()) {
                    binding.enterConfirmPasswordLayout.error = null // Clear error if it's valid
                }
            }
            if(!hasError){
                LoadingDialog.show(requireContext(), false)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val newUser = User(
                                userId = user?.uid,
                                email = user?.email,
                                phoneNum = null,
                                name = name,
                                fundPoints = 0.0,
                                verified = false,
                                status = "Active"
                            )
                            user?.uid?.let { uid ->
                                db.collection("users")
                                    .document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $uid")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(ContentValues.TAG, "Error adding document", e)
                                    }
                            }
                            val loginBottomSheetDialogFragment =
                                LoginBottomSheetDialogFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("email", email)
                                        putString("password", password)
                                    }
                                }
                            //Close Loading dialog
                            LoadingDialog.dismiss()
                            dismiss()
                            loginBottomSheetDialogFragment.show(
                                parentFragmentManager,
                                loginBottomSheetDialogFragment.tag
                            )
                            Toast.makeText(context, "Registration Successful.", Toast.LENGTH_SHORT).show()
                        } else {
                            LoadingDialog.dismiss()
                            Toast.makeText(
                                context,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.enterEmailNumLayout.error = "Enter a different email."
                        }
                    }
            }
        }

        //Code to adjust the screen when inputting/typing
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

        //Code to close keyboard when clicking outside the input fields
        binding.mainLayout.setOnClickListener {
            HideKeyboardOnClick.hideKeyboardOnClick(it)
        }

        addTextWatcher(binding.enterEmailNum, binding.enterEmailNumLayout)
        addTextWatcher(binding.enterName, binding.enterNameLayout)
        addTextWatcher(binding.enterPassword, binding.enterPasswordLayout)
        addTextWatcher(binding.enterConfirmPassword, binding.enterConfirmPasswordLayout)
    }

    private fun addTextWatcher(editText: TextInputEditText, layout: TextInputLayout) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                layout.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
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
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val newUser = User(
                        userId = user?.uid,
                        email = user?.email,
                        phoneNum = null,
                        name = user?.displayName.toString(),
                        fundPoints = 0.0,
                        verified = false,
                        status = "Active"
                    )

                    user?.uid?.let { uid ->
                        // Check if the user document already exists
                        Firebase.firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Document already exists, handle error
                                    Toast.makeText(context, "This Google account is already registered.", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Document does not exist, create a new user document
                                    Firebase.firestore.collection("users")
                                        .document(uid)
                                        .set(newUser)
                                        .addOnSuccessListener {
                                            Log.d("Firebase", "DocumentSnapshot added with ID: $uid")
                                            Toast.makeText(context, "Google Sign-In Successful.", Toast.LENGTH_SHORT).show()
                                            dismiss()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("Firebase", "Error adding document", e)
                                            Toast.makeText(context, "Error adding document: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firebase", "Error checking document existence", e)
                                Toast.makeText(context, "Error checking document existence: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val loginBottomSheetDialogFragment = LoginBottomSheetDialogFragment()
        dismiss()
        loginBottomSheetDialogFragment.show(
            parentFragmentManager,
            loginBottomSheetDialogFragment.tag
        )
    }
}