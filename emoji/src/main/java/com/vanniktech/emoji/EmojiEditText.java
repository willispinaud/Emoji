package com.vanniktech.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.annotation.CallSuper;
import android.support.annotation.DimenRes;
import android.support.annotation.Px;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import com.vanniktech.emoji.emoji.Emoji;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reference implementation for an EditText with emoji support. */
@SuppressWarnings("CPD-START") public class EmojiEditText extends AppCompatEditText implements EmojiEditTextInterface {
  private float emojiSize;

  public EmojiEditText(final Context context) {
    this(context, null);
  }

  public EmojiEditText(final Context context, final AttributeSet attrs) {
    super(context, attrs);

    if (!isInEditMode()) {
      EmojiManager.getInstance().verifyInstalled();
    }

    final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
    final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;

    if (attrs == null) {
      emojiSize = defaultEmojiSize;
    } else {
      final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EmojiEditText);

      try {
        emojiSize = a.getDimension(R.styleable.EmojiEditText_emojiSize, defaultEmojiSize);
      } finally {
        a.recycle();
      }
    }
    setText(getText());
  }

  @Override @CallSuper protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
    final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
    final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
    EmojiManager.getInstance().replaceWithImages(getContext(), getText(), emojiSize, defaultEmojiSize);
  }

  public Editable getTextReplaced(){
    String textOrigin = getText().toString();
    String pattern = ":[^\\s]*?:";
    Matcher m = Pattern.compile(pattern)
            .matcher(textOrigin);
    while (m.find()) {
      textOrigin = textOrigin.replace(m.group(), String.valueOf(m.group().hashCode()));
    }
    return new Editable.Factory().newEditable(textOrigin);
  }

  @Override public float getEmojiSize() {
    return emojiSize;
  }

  @Override @CallSuper public void backspace() {
    final KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
    dispatchKeyEvent(event);
  }

  @Override @CallSuper public void input(final Emoji emoji) {
    if (emoji != null) {
      final int start = getSelectionStart();
      final int end = getSelectionEnd();

      if (start < 0) {
        append(emoji.getUnicode());
      } else {
        getText().replace(Math.min(start, end), Math.max(start, end), emoji.getUnicode(), 0, emoji.getUnicode().length());
      }
    }
  }

  @Override public final void setEmojiSize(@Px final int pixels) {
    setEmojiSize(pixels, true);
  }

  @Override public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
    emojiSize = pixels;

    if (shouldInvalidate) {
      setText(getText());
    }
  }

  @Override public final void setEmojiSizeRes(@DimenRes final int res) {
    setEmojiSizeRes(res, true);
  }

  @Override public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
    setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
  }
}
