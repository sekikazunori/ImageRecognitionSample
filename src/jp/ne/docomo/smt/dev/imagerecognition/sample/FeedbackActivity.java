package jp.ne.docomo.smt.dev.imagerecognition.sample;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.imagerecognition.ImageRecognitionFeedback;
import jp.ne.docomo.smt.dev.imagerecognition.param.ImageRecognitionFeedbackRequestParam;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * 認識結果の候補全体に対するフィードバック画面
 * 認識結果の候補全体に対するフィードバックを実行するサンプルページ
 */
public class FeedbackActivity extends Activity implements OnClickListener  {

	private FeedbackAsyncTask task;
	
	// 結果表示
	private EditText _resultText;
	
	private class FeedbackAsyncTask extends AsyncTask<ImageRecognitionFeedbackRequestParam, Integer, Boolean> {
		private AlertDialog.Builder _dlg;
		
		private boolean isSdkException = false;
		private String exceptionMessage = null;
	 
	    public FeedbackAsyncTask(AlertDialog.Builder dlg) {
	        super();
	        _dlg = dlg;
	    }
	 
		@Override
		protected Boolean doInBackground(ImageRecognitionFeedbackRequestParam... params) {
			ImageRecognitionFeedbackRequestParam requestParam = params[0];
			try {
				// フィードバックのリクエスト送信
				ImageRecognitionFeedback feedback = new ImageRecognitionFeedback();
				feedback.request(requestParam);

			} catch (SdkException ex) {
				isSdkException = true;
				exceptionMessage = "ErrorCode: " + ex.getErrorCode() + "\nMessage: " + ex.getMessage();
				
			} catch (ServerException ex) {
				exceptionMessage = "ErrorCode: " + ex.getErrorCode() + "\nMessage: " + ex.getMessage();
			}
			return Boolean.TRUE;
	    }
		

		@Override
		protected void onCancelled() {
		}
		
	    @Override
	    protected void onPostExecute(Boolean result) {
			
	    	if(exceptionMessage != null){
	    		if(isSdkException){
	    			_dlg.setTitle("SdkException 発生");
	    		}else{
	    			_dlg.setTitle("ServerException 発生");
	    		}
				_dlg.setMessage(exceptionMessage + " ");
				_dlg.show();
	    	}else{
		    	// 結果表示
				_dlg.setTitle("処理結果");
				_resultText.setText("正常に終了しました。");

				// ソフトキーボードを非表示にする
	            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(_resultText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    	}
	    }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		_resultText = (EditText)findViewById(R.id.edit_result);
		
		// ボタンイベントの設定
		Button btn = (Button)findViewById(R.id.button_exec);
		btn.setOnClickListener(this);

		// 認識ジョブIDを受取り EditText に設定する
		Intent intent =  this.getIntent();
		String recognitionId = intent.getStringExtra(RecognitionActivity.INTENT_RECOGNITIONID_KEY);
		EditText editText = (EditText)findViewById(R.id.edit_recognitionid);
		editText.setText(recognitionId);
	}

    @Override
    public void onPause(){
    	super.onPause();
    	if(task != null){
    		task.cancel(true);
    	}
    }
    
   
	// インターフェース実装
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_exec:
			pushExecButton();
			break;
		}
	}


	private void pushExecButton() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(FeedbackActivity.this);
		ImageRecognitionFeedbackRequestParam requestParam = new ImageRecognitionFeedbackRequestParam();

		// パラメータ取得
		// 認識ジョブの識別ID
		EditText editText = (EditText)findViewById(R.id.edit_recognitionid);
		requestParam.setRecognitionId(editText.getText().toString());

		// 認識結果の妥当性
		RadioButton radio = (RadioButton)findViewById(R.id.radio_true);
		Boolean valid = Boolean.TRUE;
		if (radio.isChecked() == false) {
			valid = Boolean.FALSE;
		}
		requestParam.setValid(valid);

		// コメント
		editText = (EditText)findViewById(R.id.edit_comment);
		requestParam.setComment(editText.getText().toString());

		// 実行
		task = new FeedbackAsyncTask(dlg);
		task.execute(requestParam);
	}
}
