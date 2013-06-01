/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.special_lists_settings;

import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ViewSwitcher;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.TextView;
import de.azapps.mirakel.Mirakel;
import de.azapps.mirakel.R;
import de.azapps.mirakel.helper.ListDialogHelpers;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;

public class SpecialListSettingsActivity extends Activity {
	public static final String SLIST_ID = "de.azapps.mirakel.SpecialListSettings/list_id";
	private static final String TAG = "SpecialListSettingsActivity";
	private List<ListMirakel> lists;
	private SpecialList specialList;
	Context ctx = this;
	protected AlertDialog alert;
	private boolean[] mSelectedItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		specialList = SpecialList.getSpecialList(i.getIntExtra(SLIST_ID, 1));
		setContentView(R.layout.special_list_preferences);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(specialList.getName());

		ViewSwitcher s = (ViewSwitcher) findViewById(R.id.switch_special_list_name);
		if (s.getNextView().getId() != R.id.special_list_name_edit) {
			s.showPrevious();
		}

		final TextView name = (TextView) findViewById(R.id.special_list_name);
		name.setText(specialList.getName());
		name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.switch_special_list_name);
				switcher.showNext();
				EditText txt = (EditText) findViewById(R.id.special_list_name_edit);
				txt.setText(name.getText());
				txt.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(txt, InputMethodManager.SHOW_IMPLICIT);
				txt.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							specialList.setName(v.getText().toString());
							specialList.save();
							InputMethodManager imm = (InputMethodManager) getApplication()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(name.getWindowToken(),
									0);
							switcher.showPrevious();
							return true; // consume.
						}
						return false; // pass on to other listeners.
					}
				});
			}
		});

		CheckBox active = (CheckBox) findViewById(R.id.special_list_active);
		active.setChecked(specialList.isActive());
		active.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				specialList.setActive(isChecked);
				specialList.save();
			}
		});
		
		((TextView) findViewById(R.id.special_list_where)).setText(specialList
				.getWhereQuery());
		if(!Mirakel.DEBUG)
			((TextView) findViewById(R.id.special_list_where)).setVisibility(View.GONE);
		lists = ListMirakel.all(false);
		LinearLayout sortBy = (LinearLayout) findViewById(R.id.special_list_sort_by_view);
		final TextView sortByShow = (TextView) findViewById(R.id.special_list_sort_by_pref);
		sortByShow.setText(getResources().getStringArray(
				R.array.task_sorting_items)[specialList.getSortBy()]);
		sortBy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				specialList = (SpecialList) ListDialogHelpers.handleSortBy(ctx,
						specialList, sortByShow);
			}
		});
		LinearLayout defList = (LinearLayout) findViewById(R.id.special_list_def_list_view);
		final TextView defListShow = (TextView) findViewById(R.id.special_list_def_list_pref);
		defListShow.setText(specialList.getDefaultList().getName());
		defList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				specialList = ListDialogHelpers.handleDefaultList(ctx,
						specialList, lists, defListShow);
			}
		});

		LinearLayout defDate = (LinearLayout) findViewById(R.id.special_list_def_date_view);
		final TextView defDateShow = (TextView) findViewById(R.id.special_list_def_date_pref);
		int[] values = getResources().getIntArray(
				R.array.special_list_def_date_picker_val);
		for (int j = 0; j < values.length; j++) {
			if (specialList.getDefaultDate() == null)
				defDateShow.setText(getResources().getStringArray(
						R.array.special_list_def_date_picker)[0]);
			else if (values[j] == specialList.getDefaultDate()) {
				defDateShow.setText(getResources().getStringArray(
						R.array.special_list_def_date_picker)[j]);
			}
		}
		defDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				specialList = ListDialogHelpers.handleDefaultDate(ctx,
						specialList, defDateShow);
			}
		});

		// Where
		((TextView) findViewById(R.id.special_list_where_name_content))
				.setText(getFieldText("name"));
		((TextView) findViewById(R.id.special_list_where_list_content))
				.setText(getFieldText("list_id"));
		((TextView) findViewById(R.id.special_list_where_done_content))
				.setText(getFieldText("done"));
		((TextView) findViewById(R.id.special_list_where_content_content))
				.setText(getFieldText("content"));
		((TextView) findViewById(R.id.special_list_where_prio_content))
				.setText(getFieldText("priority"));
		((TextView) findViewById(R.id.special_list_where_due_content))
				.setText(getFieldText("due"));
		((TextView) findViewById(R.id.special_list_where_reminder_content))
				.setText(getFieldText("reminder"));
		((LinearLayout) findViewById(R.id.special_list_where_done))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final CharSequence[] SortingItems = {
								getString(R.string.nothing),
								getString(R.string.done),
								getString(R.string.undone) };
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						builder.setTitle(ctx.getString(R.string.select_by));
						int defVal = 0;
						if (specialList.getWhereQuery().contains("done=0"))
							defVal = 2;
						else if (specialList.getWhereQuery().contains("done=1"))
							defVal = 1;
						builder.setSingleChoiceItems(SortingItems, defVal,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										String newWhere = "";
										switch (item) {
										case 1:
											newWhere = "done=1";
											((TextView) findViewById(R.id.special_list_where_done_content))
													.setText(R.string.done);
											break;
										case 2:
											newWhere = "done=0";
											((TextView) findViewById(R.id.special_list_where_done_content))
													.setText(getString(R.string.undone));
											break;
										default:
											((TextView) findViewById(R.id.special_list_where_done_content))
													.setText(getString(R.string.empty));
											break;
										}
										updateWhere("done", newWhere);
										alert.dismiss(); // Ugly
									}

								});
						alert = builder.create();
						alert.show();
					}
				});
		((LinearLayout) findViewById(R.id.special_list_where_reminder))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final CharSequence[] SortingItems = {
								getString(R.string.nothing),
								getString(R.string.reminder_set),
								getString(R.string.reminder_unset) };
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						builder.setTitle(ctx.getString(R.string.select_by));
						int defVal = 0;
						if (specialList.getWhereQuery().contains("not"))
							defVal = 1;
						else if (specialList.getWhereQuery().contains(
								"reminder"))
							defVal = 2;
						builder.setSingleChoiceItems(SortingItems, defVal,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										String newWhere = "";
										switch (item) {
										case 1:
											newWhere = "reminder is not null";
											((TextView) findViewById(R.id.special_list_where_reminder_content))
													.setText(R.string.reminder_set);
											break;
										case 2:
											newWhere = "reminder is null";
											((TextView) findViewById(R.id.special_list_where_reminder_content))
													.setText(getString(R.string.reminder_unset));
											break;
										default:
											((TextView) findViewById(R.id.special_list_where_reminder_content))
													.setText(getString(R.string.empty));
											break;
										}
										updateWhere("reminder", newWhere);
										alert.dismiss(); // Ugly
									}

								});
						alert = builder.create();
						alert.show();
					}
				});

		((LinearLayout) findViewById(R.id.special_list_where_list))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						List<ListMirakel> lists = ListMirakel.all(false);
						final CharSequence[] SortingItems = new String[lists
								.size() + 1];
						final int[] values = new int[lists.size() + 1];
						mSelectedItems = new boolean[SortingItems.length];
						values[0] = 0;
						mSelectedItems[0] = specialList.getWhereQuery().contains("not list_id");
						SortingItems[0] = getString(R.string.inverte);
						for (int i = 0; i < lists.size(); i++) {
							values[i + 1] = lists.get(i).getId();
							SortingItems[i + 1] = lists.get(i).getName();
							mSelectedItems[i+1]=false;
							mSelectedItems[i + 1] = (specialList.getWhereQuery().contains(","+lists.get(i).getId())||specialList.getWhereQuery().contains("("+lists.get(i).getId()))
									&&(specialList.getWhereQuery().contains(lists.get(i).getId()+",")||specialList.getWhereQuery().contains(lists.get(i).getId()+")"));
						}
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ctx);
						builder.setTitle(ctx.getString(R.string.select_by));
						builder.setPositiveButton(R.string.OK,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										String newWhere=(mSelectedItems[0]?"not ":"")+"list_id in(";
										String text=(mSelectedItems[0]?getString(R.string.not_in)+" ":"");
										boolean first=true;
										for(int i=1;i<mSelectedItems.length;i++){
											if(mSelectedItems[i]){
												text+=(first?"":", ")+SortingItems[i];
												newWhere+=(first?"":",")+values[i];
												first=false;
											}
										}
										updateWhere("list_id", (first?"":newWhere+")"));
										((TextView) findViewById(R.id.special_list_where_list_content)).setText(first?getString(R.string.empty):text);
									}
								});
						builder.setNegativeButton(R.string.Cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										// User cancelled the dialog
									}
								});

						builder.setMultiChoiceItems(SortingItems, mSelectedItems,
								new OnMultiChoiceClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which, boolean isChecked) {
										 mSelectedItems[which]=isChecked;
									}

								});
						alert = builder.create();
						alert.show();
					}
				});
		
		((LinearLayout) findViewById(R.id.special_list_where_prio))
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] SortingItems = new String[6];
				final int[] values = new int[SortingItems.length];
				mSelectedItems = new boolean[SortingItems.length];
				values[0] = -5;
				mSelectedItems[0] = specialList.getWhereQuery().contains("not priority");
				SortingItems[0] = getString(R.string.inverte);
				for (int i = 1; i < SortingItems.length; i++) {
					SortingItems[i]=(i-3)+"";
					values[i]=(i-3);
					mSelectedItems[i]=false;
				}
				String[]p=specialList.getWhereQuery().split("and");
				for(String s:p){
					if(s.contains("priority")){
						String[] r = s.replace((!mSelectedItems[0]?"":"not ")+"priority in (", "").replace(")", "").trim().split(",");
						for(String t:r){
							try {
								switch(Integer.parseInt(t)){
									case -2:mSelectedItems[1]=true;break;
									case -1:mSelectedItems[2]=true;break;
									case  0:mSelectedItems[3]=true;break;
									case  1:mSelectedItems[4]=true;break;
									case  2:mSelectedItems[5]=true;break;
									}
							} catch (NumberFormatException e) {
							}							
						}
						break;
					}
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ctx);
				builder.setTitle(ctx.getString(R.string.select_by));
				builder.setPositiveButton(R.string.OK,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								String newWhere=(mSelectedItems[0]?"not ":"")+"priority in (";
								String text=(mSelectedItems[0]?getString(R.string.not_in)+" ":"");
								boolean first=true;
								for(int i=1;i<mSelectedItems.length;i++){
									if(mSelectedItems[i]){
										text+=(first?"":", ")+values[i];
										newWhere+=(first?"":",")+values[i];
										first=false;
									}
								}
								updateWhere("priority", (first?"":newWhere+")"));
								((TextView) findViewById(R.id.special_list_where_prio_content)).setText(first?getString(R.string.empty):text);
							}
						});
				builder.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								// User cancelled the dialog
							}
						});

				builder.setMultiChoiceItems(SortingItems, mSelectedItems,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								 mSelectedItems[which]=isChecked;
							}

						});
				alert = builder.create();
				alert.show();
			}
		});
		((LinearLayout)findViewById(R.id.special_list_where_content)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ctx);
				builder.setTitle(ctx.getString(R.string.select_by));
				builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				final View dialogView = getLayoutInflater().inflate(R.layout.content_name_dialog,null);
				builder.setView(dialogView);
				String[]p=specialList.getWhereQuery().split("and");
				((RadioButton)dialogView.findViewById(R.id.where_like_contain)).setChecked(true);
				for(String s:p){
					if(s.contains("content")){
						if(s.contains("not")){
							s=s.replace("not", "").trim();
							((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).setChecked(true);
						}
						s = s.replace("content like", "").replace("'", "").trim();
						if(s.charAt(0)=='%'){
							if(s.charAt(s.length()-1)=='%'){
								((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
								((RadioButton)dialogView.findViewById(R.id.where_like_contain)).setChecked(true);
							}else{
								((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
								((RadioButton)dialogView.findViewById(R.id.where_like_begin)).setChecked(true);
							}
						}else if(s.charAt(s.length()-1)=='%'){
							((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
							((RadioButton)dialogView.findViewById(R.id.where_like_end)).setChecked(true);
						}
						break;						
					}
				}
				builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newWhere=(((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).isChecked()?"not ":"")+"content like ";
						String text=(((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).isChecked()?getString(R.string.not)+" ":"");
						String t=((EditText)dialogView.findViewById(R.id.where_like)).getText().toString();
						switch (((RadioGroup)dialogView.findViewById(R.id.where_like_radio)).getCheckedRadioButtonId()) {
						case R.id.where_like_begin:
							newWhere+="'"+t+"%'";
							text+=getString(R.string.where_like_begin_text)+" "+t;
							break;
						case R.id.where_like_end:
							newWhere+="'%"+t+"'";
							text+=getString(R.string.where_like_end_text)+" "+t;
							break;
						default:
							newWhere+="'%"+t+"%'";
							text+=getString(R.string.where_like_contain_text)+" "+t;
							break;
						}
						updateWhere("content", newWhere);
						((TextView) findViewById(R.id.special_list_where_content_content)).setText(text);
					}
				});

				alert = builder.create();
				alert.show();
				
			}
		});
		
		((LinearLayout)findViewById(R.id.special_list_where_name)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ctx);
				builder.setTitle(ctx.getString(R.string.select_by));
				builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				final View dialogView = getLayoutInflater().inflate(R.layout.content_name_dialog,null);
				builder.setView(dialogView);
				String[]p=specialList.getWhereQuery().split("and");
				((RadioButton)dialogView.findViewById(R.id.where_like_contain)).setChecked(true);
				for(String s:p){
					if(s.contains("name")){
						if(s.contains("not")){
							s=s.replace("not", "").trim();
							((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).setChecked(true);
						}
						s = s.replace("name like", "").replace("'", "").trim();
						if(s.charAt(0)=='%'){
							if(s.charAt(s.length()-1)=='%'){
								((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
								((RadioButton)dialogView.findViewById(R.id.where_like_contain)).setChecked(true);
							}else{
								((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
								((RadioButton)dialogView.findViewById(R.id.where_like_begin)).setChecked(true);
							}
						}else if(s.charAt(s.length()-1)=='%'){
							((EditText)dialogView.findViewById(R.id.where_like)).setText(s.replace("%", ""));
							((RadioButton)dialogView.findViewById(R.id.where_like_end)).setChecked(true);
						}
						break;						
					}
				}
				builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newWhere=(((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).isChecked()?"not ":"")+"name like ";
						String text=(((CheckBox)dialogView.findViewById(R.id.where_like_inverte)).isChecked()?getString(R.string.not)+" ":"");
						String t=((EditText)dialogView.findViewById(R.id.where_like)).getText().toString();
						switch (((RadioGroup)dialogView.findViewById(R.id.where_like_radio)).getCheckedRadioButtonId()) {
						case R.id.where_like_begin:
							newWhere+="'"+t+"%'";
							text+=getString(R.string.where_like_begin_text)+" "+t;
							break;
						case R.id.where_like_end:
							newWhere+="'%"+t+"'";
							text+=getString(R.string.where_like_end_text)+" "+t;
							break;
						default:
							newWhere+="'%"+t+"%'";
							text+=getString(R.string.where_like_contain_text)+" "+t;
							break;
						}
						updateWhere("name", newWhere);
						((TextView) findViewById(R.id.special_list_where_name_content)).setText(text);
					}
				});

				alert = builder.create();
				alert.show();
				
			}
		});
		((LinearLayout)findViewById(R.id.special_list_where_due)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ctx);
				builder.setTitle(ctx.getString(R.string.select_by));
				builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				builder.setNeutralButton(R.string.no_date, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateWhere("due", "");
						
					}
				});
				final View dialogView=getLayoutInflater().inflate(R.layout.due_dialog, null);
				builder.setView(dialogView);
				((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values));
				((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setMaxValue(getResources().getStringArray(R.array.due_day_year_values).length-1);
				final String[] s =new String[100];
				for(int i=0;i<s.length;i++){
					s[i]=(i>10?"+":"")+(i-10)+"";
				}
				
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setMaxValue(s.length-1);
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setValue(10);
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setMinValue(0);
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setDisplayedValues(s);
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setWrapSelectorWheel(false);

				String[]p=specialList.getWhereQuery().split("and");
				for(String r:p){
					if(r.contains("date(due)")){
						if(r.split("\"")[3].contains("localtime")){
							((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values));
							((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setMaxValue(getResources().getStringArray(R.array.due_day_year_values).length-1);
							((NumberPicker)dialogView.findViewById(R.id.due_val)).setDisplayedValues(s);
							((NumberPicker)dialogView.findViewById(R.id.due_val)).setMaxValue(s.length-1);
							((NumberPicker)dialogView.findViewById(R.id.due_val)).setValue(10);
						}else{
							r=r.split("\"")[3];
							int day=0;
							if(r.contains("year")){
								r=r.replace((r.contains("years")?"years":"year"), "").trim();
								day=2;
							}else if(r.contains("month")){
								r=r.replace((r.contains("months")?"months":"month"), "").trim();
								day=1;
							}else{
								r=r.replace((r.contains("days")?"days":"day"), "").trim();
							}
							if(r.charAt(0)=='+')
								r=r.replace("+", "");
							int val=0;
							try {
								val=Integer.parseInt(r);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
							if(val==1||val==-1){
								((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values));
							}else{
								((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values_plural));
							}
							Log.e(TAG,""+val);
							((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setValue(day);

							((NumberPicker)dialogView.findViewById(R.id.due_val)).setValue(10+val);
							//TODO fix bug on negativ values
							/*If you try to show -1 -10 is shown*/
							
							
						}
							
					}
				}
				((NumberPicker)dialogView.findViewById(R.id.due_val)).setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						if(newVal==11||newVal==9){//=1||=9
							((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values));
						}else{
							((NumberPicker)dialogView.findViewById(R.id.due_day_year)).setDisplayedValues(getResources().getStringArray(R.array.due_day_year_values_plural));
						}
						
					}
				});
		        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int val=((NumberPicker)dialogView.findViewById(R.id.due_val)).getValue();
						Log.e(TAG,"val: "+val+"| shown "+s[val]);
						String newWhere="";
						if(val==10){
							newWhere="date(due)<=date(\"now\",\"localtime\")";
							((TextView)findViewById(R.id.special_list_where_due_content)).setText(getString(R.string.today));
						}else{
							String mod="";
							switch (((NumberPicker)dialogView.findViewById(R.id.due_day_year)).getValue()) {
							case 1:
								mod=(val!=11?"month":"months");
								break;
							case 2:
								mod=(val!=11?"year":"years");
								break;
							default:
								mod=(val!=11?"day":"days");
								break;
							}
							((TextView)findViewById(R.id.special_list_where_due_content)).setText(s[val]+" "+
							getResources().getStringArray(val==11?R.array.due_day_year_values:R.array.due_day_year_values_plural)[((NumberPicker)dialogView.findViewById(R.id.due_day_year)).getValue()]);
							newWhere="date(due)<=date(\"now\",\""+s[val]+" "+mod+"\",\"localtime\")";
						}
						updateWhere("due", newWhere);
					}
				});
		        alert=builder.create();
		        alert.show();
				
			}
		});
	}

	private void updateWhere(String attr, String newWhere) {
		if (specialList.getWhereQuery().contains(attr)) {
			String[] parts = specialList.getWhereQuery().split("and");
			String n = "";
			boolean first=true;
			for (int i = 0; i < parts.length; i++) {
				boolean a=parts[i].contains(attr);
				boolean b=(((!parts[i].contains("not null"))||newWhere.isEmpty())||attr!="due");
				if (a&&b) {
					parts[i] = newWhere;
					if (newWhere.isEmpty())
						continue;
				}
				n += (first ? "" : " and ") + parts[i].trim();
				first=false;
			}
			specialList.setWhereQuery(n);
		} else if (specialList.getWhereQuery().isEmpty()) {
			specialList.setWhereQuery((attr=="due"?"due is not null and ":"")+newWhere);
		} else if(newWhere!=""){
			specialList.setWhereQuery((attr=="due"?"due is not null and ":"")+specialList.getWhereQuery() + " and "
					+ newWhere);
		}
		specialList.save();
		((TextView) findViewById(R.id.special_list_where)).setText(specialList
				.getWhereQuery());
	}

	protected String getFieldText(String queryPart) {
		String[] whereParts = specialList.getWhereQuery().split("and");
		String returnString = "";
		for (String s : whereParts) {
			if (s.contains(queryPart)) {
				if (queryPart == "done") {
					return s.contains("=1") ? getString(R.string.done)
							: getString(R.string.undone);
				}
				if (queryPart == "due") {
					if (!s.contains("due is not null")&&!s.contains("due not null")) {
						if(s.split("\"")[3].contains("localtime")){
							return getString(R.string.today);
						}else{
							s=s.split("\"")[3];
							int day=0;
							if(s.contains("year")){
								s=s.replace((s.contains("years")?"years":"year"), "").trim();
								day=2;
							}else if(s.contains("month")){
								s=s.replace((s.contains("months")?"months":"month"), "").trim();
								day=1;
							}else{
								s=s.replace((s.contains("days")?"days":"day"), "").trim();
							}
							return s.trim()+" "+getResources().getStringArray((s.contains("1")?R.array.due_day_year_values:R.array.due_day_year_values_plural))[day];
						}
					}
				}
				if (queryPart == "reminder") {
					return s.contains("not") ? getString(R.string.reminder_set)
							: getString(R.string.reminder_unset);
				} if(queryPart=="list_id"){
					returnString=(specialList.getWhereQuery().contains("not list_id")?getString(R.string.not_in)+" ":"");
					String[] p = s.replace((returnString==""?"":"not ")+"list_id in(", "").replace(")", "").split(",");
					for (int i=0;i<p.length;i++) {
						returnString+=(i==0?"":", ")+(ListMirakel.getList(Integer.parseInt(p[i].trim())).getName());
					}
					return returnString;
				} if(queryPart=="priority"){
					returnString=(specialList.getWhereQuery().contains("not priority")?getString(R.string.not_in)+" ":"");
					return returnString+ s.replace((returnString==""?"":"not ")+"priority in (", "").replace(")", "").replace(",", ", ");
				}if(queryPart=="content"||queryPart=="name"){
					if(s.contains("not")){
						s=s.replace("not", "").trim();
						returnString+=getString(R.string.not)+" ";
					}
					s = s.replace(queryPart+" like", "").replace("'", "").trim();
					if(s.charAt(0)=='%'){
						if(s.charAt(s.length()-1)=='%'){
							returnString+=getString(R.string.where_like_contain_text)+" "+s.replace("%", "");
						}else{
							returnString+=getString(R.string.where_like_begin_text)+" "+s.replace("%", "");
						}
					}else if(s.charAt(s.length()-1)=='%'){
						returnString+=getString(R.string.where_like_end_text)+" "+s.replace("%", "");
					}
					return returnString;
				}
				else {
					returnString += s + " ";
				}
			}
		}
		return returnString == "" ? getString(R.string.empty) : returnString;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.special_list_settingsactivity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_delete:
			specialList.destroy();
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
