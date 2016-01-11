package jp.ne.docomo.smt.dev.imagerecognition.sample;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.imagerecognition.ImageRecognitionFeedbackToCandidate;
import jp.ne.docomo.smt.dev.imagerecognition.param.ImageRecognitionFeedbackToCandidateRequestParam;
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
public class FeedbackToCandidateActivity extends Activity implements OnClickListener  {

	private FeedbackToCandidateAsyncTask task;
	
	// 結果表示
	private EditText _resultText;
	
	private class FeedbackToCandidateAsyncTask extends AsyncTask<ImageRecognitionFeedbackToCandidateRequestParam, Integer, Boolean> {
		private AlertDialog.Builder _dlg;
		
		private boolean isSdkException = false;
		private String exceptionMessage = null;
	 
	    public FeedbackToCandidateAsyncTask(AlertDialog.Builder dlg) {
	        super();
	        _dlg = dlg;
	    }
	 
		@Override
		protected Boolean doInBackground(ImageRecognitionFeedbackToCandidateRequestParam... params) {
			ImageRecognitionFeedbackToCandidateRequestParam requestParam = params[0];
			try {
				// フィードバックのリクエスト送信
				ImageRecognitionFeedbackToCandidate feedback = new ImageRecognitionFeedbackToCandidate();
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
		setContentView(R.layout.activity_candidate);

		_resultText = (EditText)findViewById(R.id.edit_result);
		
		// ボタンイベントの設定
		Button btn = (Button)findViewById(R.id.button_exec);
		btn.setOnClickListener(this);

		// 認識ジョブIDを受取り EditText に設定する
		Intent intent =  this.getIntent();
		String recognitionId = intent.getStringExtra(RecognitionActivity.INTENT_RECOGNITIONID_KEY);
		EditText editText = (EditText)findViewById(R.id.edit_recognitionid);
		editText.setText(recognitionId);

		// フィードバック対象商品Idを受取り EditText に設定する
		String itemId = intent.getStringExtra(RecognitionActivity.INTENT_ITEMID_KEY);
		editText = (EditText)findViewById(R.id.edit_itemid);
		editText.setText(itemId);

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
		AlertDialog.Builder dlg = new AlertDialog.Builder(FeedbackToCandidateActivity.this);
		ImageRecognitionFeedbackToCandidateRequestParam requestParam =
											new ImageRecognitionFeedbackToCandidateRequestParam();

		// パラメータ取得
		// 認識ジョブの識別ID
		EditText editText = (EditText)findViewById(R.id.edit_recognitionid);
		requestParam.setRecognitionId(editText.getText().toString());

		// フィードバック対象商品ID
		editText = (EditText)findViewById(R.id.edit_itemid);
		requestParam.setItemId(editText.getText().toString());

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
		task = new FeedbackToCandidateAsyncTask(dlg);
		task.execute(requestParam);
	}
}
