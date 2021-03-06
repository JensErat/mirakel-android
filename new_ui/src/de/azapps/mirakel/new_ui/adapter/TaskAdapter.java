/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 *  Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package de.azapps.mirakel.new_ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import de.azapps.mirakel.helper.DateTimeHelper;
import de.azapps.mirakel.helper.TaskHelper;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.new_ui.R;
import de.azapps.mirakel.new_ui.interfaces.OnTaskSelectedListener;
import de.azapps.mirakel.new_ui.views.ProgressDoneView;
import de.azapps.mirakel.new_ui.views.TaskNameView;

public class TaskAdapter extends CursorAdapter<TaskAdapter.TaskViewHolder> {

    private final LayoutInflater mInflater;
    private final OnTaskSelectedListener itemClickListener;

    public TaskAdapter(final Context context, final Cursor cursor, final int flags,
                       final OnTaskSelectedListener itemClickListener) {
        super(context, cursor, flags);
        mInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
        setHasStableIds(true);
    }



    @Override
    public TaskViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
        final View view = mInflater.inflate(R.layout.row_task, viewGroup, false);
        return new TaskViewHolder(view);
    }


    @Override
    protected void onContentChanged() {
        //nothing for now
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final Cursor cursor) {
        final Task task = new Task(cursor);
        holder.task = task;
        holder.name.setText(task.getName());
        holder.name.setStrikeThrough(task.isDone());
        if (task.getDue().isPresent()) {
            holder.due.setVisibility(View.VISIBLE);
            holder.due.setText(DateTimeHelper.formatDate(mContext,
                               task.getDue()));
            holder.due.setTextColor(TaskHelper.getTaskDueColor(mContext, task.getDue(),
                                    task.isDone()));
        } else {
            holder.due.setVisibility(View.GONE);
        }
        holder.list.setText(task.getList().getName());
        holder.priorityDone.setChecked(task.isDone());
        holder.priorityDone.setProgress(task.getProgress());
        holder.priorityDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                final Task task = holder.getTask();
                task.setDone(isChecked);
                task.save();
            }
        });
    }



    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TaskNameView name;
        final TextView due;
        final TextView list;
        final ProgressDoneView priorityDone;
        private Task task;

        public TaskViewHolder(final View view) {
            super(view);
            view.setOnClickListener(this);
            name = (TaskNameView) view.findViewById(R.id.task_name);
            due = (TextView) view.findViewById(R.id.task_due);
            list = (TextView) view.findViewById(R.id.task_list);
            priorityDone = (ProgressDoneView) view.findViewById(R.id.task_priority_done);
        }

        public Task getTask() {
            return task;
        }

        @Override
        public void onClick(final View v) {
            itemClickListener.onTaskSelected(task);
        }
    }
}
