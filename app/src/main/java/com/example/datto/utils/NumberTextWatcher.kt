import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat

class NumberTextWatcher(private val editText: EditText) : TextWatcher {

    private var current = ""

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.replace("[,]".toRegex(), "")
            val parsed = if (cleanString.isEmpty()) 0.0 else cleanString.toDouble()
            val formatted = DecimalFormat("#,###").format(parsed)

            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)

            editText.addTextChangedListener(this)
        }
    }

    override fun afterTextChanged(s: Editable) {}
}