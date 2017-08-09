package com.cityparking.pratteln.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.cityparking.pratteln.R;

@SuppressLint("NewApi")
public class TermsFragment extends Fragment {

//	private WebSettings webSettings;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_terms, container, false);

		String termsText = "<html><body style=\"padding:0; margin:0;\"><font color=\"";
		termsText += "#343432";
		termsText += "\"><p align=\"justify\">";
		termsText += getString(R.string.terms_conditions_info);
		termsText += "</p></font></body></html>";

		WebView termsWebView = (WebView) view.findViewById(R.id.terms_conditions_info);
		termsWebView.setBackgroundColor(0x00000000);

		termsWebView.loadData(termsText, "text/html", "utf-8");
		return view;
	}

}
