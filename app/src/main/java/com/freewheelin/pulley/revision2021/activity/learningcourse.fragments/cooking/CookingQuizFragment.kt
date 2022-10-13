package com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.cooking
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.ViewModelProvider
//import com.freewheelin.pulley.revision2021.activity.learningcourse.fragments.LCCookingFragment
//import com.freewheelin.pulley.R
//import com.freewheelin.pulley.databinding.FragmentCookingQuizBinding
//import com.freewheelin.pulley.databinding.ItemCookingQuizDetailBinding
//import com.freewheelin.pulley.revision2021.model.CookingExercise
//import com.freewheelin.pulley.revision2021.model.CookingQuiz
//import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.cooking.CookingQuizViewModel
//import com.freewheelin.pulley.revision2021.views.CookingQuizAnswerSelectDialog
//import java.io.Serializable
//
//// Deprecated
//class CookingQuizFragment() : Fragment() {
//    companion object {
//        val PARAM_QUIZ = "QUIZ"
//        val PARAM_EXER = "EXER"
//
//        fun newInstance(exercise: CookingExercise): CookingQuizFragment {
//            return CookingQuizFragment().apply {
//                arguments = Bundle().apply {
//                    putSerializable(PARAM_EXER, exercise as Serializable)
//                }
//            }
//            return CookingQuizFragment()
//        }
//    }
//    val binding: FragmentCookingQuizBinding by lazy {
//        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_cooking_quiz, null, false)
//    }
//
//    private lateinit var viewModel: CookingQuizViewModel
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this).get(CookingQuizViewModel::class.java)
//
//        arguments?.let {
//            val exercise = it.getSerializable(PARAM_EXER) as CookingExercise
//            binding.apply {
////                vm = viewModel
//                lifecycleOwner = viewLifecycleOwner
//
//                viewModel.initCookingExercise(exercise)
//
//                listOfNotNull(
//                    viewModel.cookingQuiz0.value,
//                    viewModel.cookingQuiz1.value,
//                    viewModel.cookingQuiz2.value,
//                    viewModel.cookingQuiz3.value,
//                    viewModel.cookingQuiz4.value,
//                ).forEachIndexed { index, quiz ->
//                    val itemBinding = getDetailBindingOnIndex(index)
//                    setOnDetailView(itemBinding, quiz)
//                }
//            }
//        }
//    }
//
//    private fun setOnDetailView (itemBinding: ItemCookingQuizDetailBinding, quiz: CookingQuiz) {
//        itemBinding.let {
//            it.hintBtn.setOnClickListener { view ->
//                (parentFragment as LCCookingFragment).setHintImageToCooking(quiz.hintImageUrl)
//            }
//            it.quizSingleAnswer.setOnClickListener { view ->
//                onSingleAnswerClick(quiz, view)
//            }
//        }
//    }
//
//    private fun onSingleAnswerClick (quiz: CookingQuiz, sourceView: View) {
//        if (quiz.isAnswerEntered.get()) {
//            return
//        }
//
//        val selectionImages = quiz.answerOptions.map { it.imageUrl }
//        val dialog = CookingQuizAnswerSelectDialog(requireContext(), sourceView, selectionImages) { selectedContent ->
//            // TODO 이미지 클릭시 적용하는것
//            quiz.selectedQuizAnswerImageUrl.set(selectedContent.imageUrl)
//            quiz.isAnswerEntered.set(true)
//            val isCorrect = quiz.answer == "${selectedContent.seq}"
//            quiz.isCorrectAnswer.set(isCorrect)
//        }
//        childFragmentManager.let { dialog.show(it, "CookingQuizAnswerSelectDialog") }
//    }
//    private fun getDetailBindingOnIndex(index: Int): ItemCookingQuizDetailBinding {
//        return when (index) {
//            0 -> binding.quizDetail0
//            1 -> binding.quizDetail1
//            2 -> binding.quizDetail2
//            3 -> binding.quizDetail3
//            4 -> binding.quizDetail4
//            else -> binding.quizDetail0
//        }
//    }
//
//}