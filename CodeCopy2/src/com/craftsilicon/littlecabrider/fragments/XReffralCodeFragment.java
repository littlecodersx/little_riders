/**
 * 
 */
package com.craftsilicon.littlecabrider.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.craftsilicon.littlecabrider.R;
import com.craftsilicon.littlecabrider.component.MyFontEdittextView;
import com.craftsilicon.littlecabrider.parse.AsyncTaskCompleteListener;
import com.craftsilicon.littlecabrider.parse.HttpRequester;
import com.craftsilicon.littlecabrider.parse.ParseContent;
import com.craftsilicon.littlecabrider.utils.AndyUtils;
import com.craftsilicon.littlecabrider.utils.AppLog;
import com.craftsilicon.littlecabrider.utils.Const;
import com.craftsilicon.littlecabrider.utils.PreferenceHelper;

/**
 * @author Elluminati elluminati.in
 * 
 */
public class XReffralCodeFragment extends BaseFragmentRegister implements
		AsyncTaskCompleteListener {
	private MyFontEdittextView etRefCode;
	private String token, id;
	private LinearLayout llErrorMsg;
	private int is_skip = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.automated.taxinow.fragments.UberBaseFragmentRegister#onCreate(android
	 * .os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		token = getArguments().getString(Const.Params.TOKEN);
		id = getArguments().getString(Const.Params.ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setIconMenu(R.drawable.ic_launcher);
		activity.setTitle(getString(R.string.text_referral_code));
		activity.btnNotification.setVisibility(View.INVISIBLE);
		View refView = inflater.inflate(R.layout.ref_code_fragment, container,
				false);
		etRefCode = (MyFontEdittextView) refView.findViewById(R.id.etRefCode);
		etRefCode.setHint(getString(R.string.text_enter_ref_code));
		llErrorMsg = (LinearLayout) refView.findViewById(R.id.llErrorMsg);
		refView.findViewById(R.id.btnRefSubmit).setOnClickListener(this);
		refView.findViewById(R.id.btnSkip).setOnClickListener(this);

		return refView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		etRefCode.requestFocus();
		activity.showKeyboard(etRefCode);
		// (getResources().getString(
		// R.string.text_forget_password));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.automated.taxinow.fragments.UberBaseFragmentRegister#onResume()
	 */
	@Override
	public void onResume() {
		activity.currentFragment = Const.FRAGMENT_REFFREAL;
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRefSubmit:
			if (etRefCode.getText().length() == 0) {
				AndyUtils.showToast(
						getResources().getString(R.string.text_blank_ref_code),
						activity);
				return;
			} else {
				if (!AndyUtils.isNetworkAvailable(activity)) {
					AndyUtils
							.showToast(
									getResources().getString(
											R.string.dialog_no_inter_message),
									activity);
					return;
				}
				is_skip = 0;
				applyReffralCode(true);
			}
			break;
		case R.id.btnSkip:
			is_skip = 1;
			applyReffralCode(true);
			this.OnBackPressed();
			break;

		default:
			break;
		}
	}

	private void applyReffralCode(boolean isShowLoader) {
		if (isShowLoader)
			AndyUtils.showCustomProgressDialog(activity,
					getString(R.string.progress_loading), false, null);
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.APPLY_REFFRAL_CODE);
		map.put(Const.Params.REFERRAL_CODE, etRefCode.getText().toString());
		map.put(Const.Params.ID, id);
		map.put(Const.Params.TOKEN, token);
		map.put(Const.Params.IS_SKIP, String.valueOf(is_skip));
		new HttpRequester(activity, map, Const.ServiceCode.APPLY_REFFRAL_CODE,
				this);
		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.APPLY_REFFRAL_CODE, this, this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.uberdriverforx.parse.AsyncTaskCompleteListener#onTaskCompleted(java
	 * .lang.String, int)
	 */
	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		AppLog.Log(Const.TAG, "Apply-Referral Response ::: " + response);
		switch (serviceCode) {
		case Const.ServiceCode.APPLY_REFFRAL_CODE:
			if (new ParseContent(activity).isSuccess(response)) {
				new PreferenceHelper(activity).putReferee(1);
				gotoPaymentFragment();
				// activity.startActivity(new Intent(activity,
				// MainDrawerActivity.class));
			} else {
				llErrorMsg.setVisibility(View.VISIBLE);
				etRefCode.requestFocus();
			}
			break;

		default:
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uberorg.fragments.UberBaseFragmentRegister#isValidate()
	 */
	@Override
	protected boolean isValidate() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uberorg.fragments.UberBaseFragmentRegister#OnBackPressed()
	 */
	@Override
	public boolean OnBackPressed() {
		// is_skip = 1;
		// applyReffralCode();
		// gotoPaymentFragment();
		return true;
	}

	private void gotoPaymentFragment() {
		AddPaymentFragmentRegister paymentFragment = new AddPaymentFragmentRegister();
		Bundle bundle = new Bundle();
		bundle.putString(Const.Params.TOKEN, token);
		bundle.putString(Const.Params.ID, id);
		paymentFragment.setArguments(bundle);
		activity.addFragment(paymentFragment, false,
				Const.FRAGMENT_PAYMENT_REGISTER);
	}

}
