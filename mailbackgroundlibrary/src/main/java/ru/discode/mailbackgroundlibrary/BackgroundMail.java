package ru.discode.mailbackgroundlibrary;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.discode.mailbackgroundlibrary.util.MailSender;
import ru.discode.mailbackgroundlibrary.util.Utils;


public class BackgroundMail {
    private final String TAG = BackgroundMail.class.getSimpleName();

    private String username;
    private String password;
    private String from;
    private String senderName;
    private String mailTo;
    private String mailCc;
    private String mailBcc;
    private String subject;
    private String body;
    private String type;
    private MailSender.MailBox mailBox;
    private boolean useDefaultSession;
    private String sendingMessage;
    private ArrayList<String> attachments = new ArrayList<>();
    private Context mContext;
    private OnSendingCallback onSendingCallback;

    public final static String TYPE_PLAIN = "text/plain";
    public final static String TYPE_HTML = "text/html";

    public interface OnSendingCallback {
        void onSuccess();

        void onFail(Exception e);
    }

    public BackgroundMail(Fragment fragment) {
        this(fragment.getActivity().getApplicationContext());
    }

    public BackgroundMail(Context context) {
        this.mContext = context;
    }

    private BackgroundMail(Builder builder) {
        mContext = builder.context;
        attachments = builder.attachments;
        username = builder.username;
        password = builder.password;
        from = builder.from;
        senderName = builder.senderName;
        mailTo = builder.mailTo;
        mailCc = builder.mailCc;
        mailBcc = builder.mailBcc;
        subject = builder.subject;
        body = builder.body;
        type = builder.type;
        useDefaultSession = builder.useDefaultSession;
        mailBox = builder.mailBox;
        setSendingMessage(builder.sendingMessage);
        setOnSendingCallback(builder.onSendingCallback);
    }

    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    public static Builder newBuilder(Fragment fragment) {
        return new Builder(fragment.getActivity().getApplicationContext());
    }

    public void setGmailUserName(@NonNull String string) {
        this.username = string;
    }

    public void setGmailUserName(@StringRes int strRes) {
        this.username = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getGmailUserName() {
        return username;
    }

    public void setGmailPassword(@NonNull String string) {
        this.password = string;
    }

    public void setGmailPassword(@StringRes int strRes) {
        this.password = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getGmailPassword() {
        return password;
    }

    public void setType(@NonNull String string) {
        this.type = string;
    }

    public void setType(@StringRes int strRes) {
        this.type = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setUseDefaultSession(boolean useDefaultSession) {
        this.useDefaultSession = useDefaultSession;
    }

    public boolean isUseDefaultSession() {
        return useDefaultSession;
    }

    public void setMailTo(@NonNull String string) {
        this.mailTo = string;
    }

    public void setMailTo(@StringRes int strRes) {
        this.mailTo = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getMailTo() {
        return mailTo;
    }

    public void setMailCc(@NonNull String string) {
        this.mailCc = string;
    }

    public void setMailCc(@StringRes int strRes) {
        this.mailCc = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getMailCc() {
        return mailCc;
    }

    public void setMailBcc(@NonNull String string) {
        this.mailBcc = string;
    }

    public void setMailBcc(@StringRes int strRes) {
        this.mailBcc = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getMailBcc() {
        return mailBcc;
    }

    public void setSubject(@NonNull String string) {
        this.subject = string;
    }

    public void setSubject(@StringRes int strRes) {
        this.subject = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getSubject() {
        return subject;
    }

    public void setBody(@NonNull String string) {
        this.body = string;
    }

    public void setBody(@StringRes int strRes) {
        this.body = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getBody() {
        return body;
    }

    public void setSendingMessage(@NonNull String string) {
        this.sendingMessage = string;
    }

    public void setSendingMessage(@StringRes int strRes) {
        this.sendingMessage = mContext.getResources().getString(strRes);
    }

    @NonNull
    public String getSendingMessage() {
        return sendingMessage;
    }

    public void addAttachment(@NonNull String attachment) {
        this.attachments.add(attachment);
    }

    public void addAttachment(@StringRes int strRes) {
        this.attachments.add(mContext.getResources().getString(strRes));
    }

    public void addAttachments(@NonNull List<String> attachments) {
        this.attachments.addAll(attachments);
    }

    public void addAttachments(String... attachments) {
        this.attachments.addAll(Arrays.asList(attachments));
    }

    @NonNull
    public List<String> getAttachments() {
        return attachments;
    }

    public void setOnSendingCallback(OnSendingCallback onSendingCallback) {
        this.onSendingCallback = onSendingCallback;
    }

    public void send() {
        if(mailBox == null) {
            throw new IllegalArgumentException("You didn't set a Mailbox");
        }
        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("You didn't set a mail username");
        }
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("You didn't set a mail password");
        }
        if (TextUtils.isEmpty(mailTo) && TextUtils.isEmpty(mailCc) && TextUtils.isEmpty(mailBcc)) {
            throw new IllegalArgumentException("You didn't set any recipient addresses");
        }
        if (!Utils.isNetworkAvailable(mContext)) {
            Log.d(TAG, "you need internet connection to send the email");
        }

        new SendEmailTask().execute();
    }

    public static final class Builder {
        private Context context;
        private String username;
        private String from;
        private String password;
        private String senderName;
        private String mailTo;
        private String mailCc;
        private String mailBcc;
        private String subject = "";
        private String body = "";
        private String type = BackgroundMail.TYPE_PLAIN;
        private MailSender.MailBox mailBox;
        private boolean useDefaultSession = true;
        private ArrayList<String> attachments = new ArrayList<>();
        private String sendingMessage;
        private OnSendingCallback onSendingCallback;

        private Builder(Context context) {
            this.context = context;
        }
        public Builder withMailBox(@NonNull String smtp, Integer port) {
            this.mailBox = MailSender.buildMailBox(smtp, port);
            return this;
        }

        public Builder withMailBox(@NonNull String smtp, Integer port, Boolean ssl) {
            this.mailBox = MailSender.buildMailBox(smtp, port, ssl);
            return this;
        }

        public Builder withUsername(@NonNull String username) {
            this.username = username;
            return this;
        }

        public Builder withUsername(@StringRes int usernameRes) {
            this.username = context.getResources().getString(usernameRes);
            return this;
        }

        public Builder withFrom(@NonNull String from) {
            this.from = from;
            return this;
        }

        public Builder withFrom(@StringRes int fromRes) {
            this.from = context.getResources().getString(fromRes);
            return this;
        }

        public Builder withPassword(@NonNull String password) {
            this.password = password;
            return this;
        }

        public Builder withPassword(@StringRes int passwordRes) {
            this.password = context.getResources().getString(passwordRes);
            return this;
        }

        public Builder withSenderName(@NonNull String senderName) {
            this.senderName = senderName;
            return this;
        }

        public Builder withMailTo(@NonNull String mailTo) {
            this.mailTo = mailTo;
            return this;
        }

        public Builder withMailTo(@StringRes int mailToRes) {
            this.mailTo = context.getResources().getString(mailToRes);
            return this;
        }

        public Builder withMailCc(@NonNull String mailCc) {
            this.mailCc = mailCc;
            return this;
        }

        public Builder withMailCc(@StringRes int mailCcRes) {
            this.mailCc = context.getResources().getString(mailCcRes);
            return this;
        }

        public Builder withMailBcc(@NonNull String mailBcc) {
            this.mailBcc = mailBcc;
            return this;
        }

        public Builder withMailBcc(@StringRes int mailBccRes) {
            this.mailBcc = context.getResources().getString(mailBccRes);
            return this;
        }

        public Builder withSubject(@NonNull String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withSubject(@StringRes int subjectRes) {
            this.subject = context.getResources().getString(subjectRes);
            return this;
        }

        //set email mime type
        public Builder withType(@NonNull String type) {
            this.type = type;
            return this;
        }

        public Builder withUseDefaultSession(boolean useDefaultSession) {
            this.useDefaultSession = useDefaultSession;
            return this;
        }

        public Builder withType(@StringRes int typeRes) {
            this.type = context.getResources().getString(typeRes);
            return this;
        }

        public Builder withBody(@NonNull String body) {
            this.body = body;
            return this;
        }

        public Builder withBody(@StringRes int bodyRes) {
            this.body = context.getResources().getString(bodyRes);
            return this;
        }

        public Builder withAttachments(@NonNull ArrayList<String> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        public Builder withAttachments(String... attachments) {
            this.attachments.addAll(Arrays.asList(attachments));
            return this;
        }

        public Builder withAttachments(@ArrayRes int attachmentsRes) {
            this.attachments.addAll(Arrays.asList(context.getResources().getStringArray(attachmentsRes)));
            return this;
        }

        public Builder withSendingMessage(@NonNull String sendingMessage) {
            this.sendingMessage = sendingMessage;
            return this;
        }

        public Builder withSendingMessage(@StringRes int sendingMessageRes) {
            this.sendingMessage = context.getResources().getString(sendingMessageRes);
            return this;
        }

        public Builder withOnSuccessCallback(OnSendingCallback onSendingCallback) {
            this.onSendingCallback = onSendingCallback;
            return this;
        }

        public BackgroundMail build() {
            return new BackgroundMail(this);
        }

        public BackgroundMail send() {
            BackgroundMail backgroundMail = build();
            backgroundMail.send();
            return backgroundMail;
        }
    }

    public class SendEmailTask extends AsyncTask<String, Void, Exception> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!TextUtils.isEmpty(sendingMessage)) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setMessage(sendingMessage);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        }

        @Override
        protected Exception doInBackground(String... arg0) {
            try {
                MailSender sender = new MailSender(username, password, useDefaultSession, mailBox, from);
                if (!attachments.isEmpty()) {
                    for (int i = 0; i < attachments.size(); i++) {
                        if (!attachments.get(i).isEmpty()) {
                            sender.addAttachment(attachments.get(i));
                        }
                    }
                }
                sender.sendMail(subject, body, from, senderName, mailTo, mailCc, mailBcc, type);
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (onSendingCallback != null) {
                if (result == null) {
                    onSendingCallback.onSuccess();
                } else {
                    result.printStackTrace();
                    onSendingCallback.onFail(result);
                }
            }
        }
    }
}
