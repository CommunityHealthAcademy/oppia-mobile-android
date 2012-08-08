package org.digitalcampus.mtrain.widgets;

import java.util.List;

import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.questiontypes.*;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.widgets.mquiz.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MQuizWidget extends WidgetFactory{

	private static final String TAG = "MQuizWidget";
	
	private Context ctx;
	private MQuiz mQuiz;
	private QuestionWidget qw;
	
	public MQuizWidget(Context context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		View vv = super.getLayoutInflater().inflate(R.layout.widget_mquiz, null);
		super.getLayout().addView(vv);
		
		// TODO error check that "content" is in the hashmap
		String quiz = activity.getActivity().get("content");
		mQuiz = new MQuiz();
		mQuiz.load(quiz);
		
		this.showQuestion();
	}
	
	public void showQuestion(){
		QuizQuestion q = mQuiz.getCurrentQuestion();
		TextView qText = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questiontext);
		qText.setText(q.getQtext());
		
		if(q instanceof MultiChoice){
    		qw = new MultiChoiceWidget(this.ctx);
    	} else if(q instanceof Essay){
    		qw = new EssayWidget(this.ctx);
    	} else if(q instanceof MultiSelect){
    		qw = new MultiSelectWidget(this.ctx);
    	} else if(q instanceof ShortAnswer){
    		qw = new ShortAnswerWidget(this.ctx);
    	} else if(q instanceof Matching){
    		qw = new MatchingWidget(this.ctx);
    	} else if(q instanceof Numerical){
    		qw = new NumericalWidget(this.ctx);
    	} else {
    		Log.d(TAG,"Class for question type not found");
    		return;
    	}
		TextView qHint = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questionhint);
    	if(q.getQhint().equals("")){
    		qHint.setVisibility(View.GONE);
    	} else {
    		qHint.setText(q.getQhint());
    		qHint.setVisibility(View.VISIBLE);
    	}
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setProgress();
		this.setNav();
	}
	
	private void setNav(){
		Button prevBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_prev_btn);
		if(mQuiz.hasPrevious()){
			 prevBtn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
		        		// save answer
		        		saveAnswer();
		        		
		        		if(mQuiz.hasPrevious()){
		        			mQuiz.movePrevious();
		    				showQuestion();
		    			}
		        	}
		        });
			prevBtn.setEnabled(true);
		} else {
			prevBtn.setEnabled(false);
		}
		
		Button nextBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_next_btn);
		nextBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
	        		// save answer
	        		if (saveAnswer()){
	        			String feedback = mQuiz.getCurrentQuestion().getFeedback();
						if (!feedback.equals("")) {
							showFeedback(feedback);
						} else if (mQuiz.hasNext()) {
							mQuiz.moveNext();
							showQuestion();
						} else {
							showResults();
						}
	        		} else {
	        			CharSequence text = ((android.app.Activity) ctx).getString(R.string.widget_mquiz_noanswergiven);
	        			int duration = Toast.LENGTH_SHORT;

	        			Toast toast = Toast.makeText(ctx, text, duration);
	        			toast.show();
	        		}
	        	}
		});
	}
	
	private void setProgress(){
		TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
		progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_progress,mQuiz.getCurrentQuestionNo(),mQuiz.getTotalNoQuestions()));
		
	}
	
	private boolean saveAnswer(){
    	List<String> answers = qw.getQuestionResponses(mQuiz.getCurrentQuestion().getResponseOptions());
    	if(answers != null){
    		mQuiz.getCurrentQuestion().setUserResponses(answers);
    		return true;
    	}
		return false;
    }
	
	 private void showFeedback(String msg){
	    	AlertDialog.Builder builder = new AlertDialog.Builder((android.app.Activity) this.ctx);
	    	// TODO change to proper strings
			builder.setTitle("Feedback");
			builder.setMessage(msg);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					if(mQuiz.hasNext()){
						mQuiz.moveNext();
						showQuestion();
					} else {
						showResults();
					}
				}
		     });
			builder.show();
	    }
	 
	 private void showResults(){
		 Toast.makeText(ctx, "showing results", Toast.LENGTH_SHORT).show();
	 }

}