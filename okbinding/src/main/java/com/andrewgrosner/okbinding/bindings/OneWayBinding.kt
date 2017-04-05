package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.TextView
import com.andrewgrosner.okbinding.viewextensions.setCheckedIfNecessary
import com.andrewgrosner.okbinding.viewextensions.setTextIfNecessary
import com.andrewgrosner.okbinding.viewextensions.setTimeIfNecessary
import java.util.*

typealias BindingExpression<Input, Output> = (Input) -> Output

interface Binding<Input, Output, Converter : BindingConverter<Input>> {

    fun notifyValueChange()

    fun unbind()
}

infix fun <Input, Output, TBinding : BindingConverter<Input>> TBinding.on(expression: BindingExpression<Input, Output>)
        = OneWayExpression(this, expression)

fun <Input, TBinding : BindingConverter<Input>> TBinding.onSelf() = OneWayExpression(this, { it })

class OneWayExpression<Input, Output, Converter : BindingConverter<Input>>(
        val binding: Converter,
        val expression: BindingExpression<Input, Output>) {
    fun <V : View> toView(view: V, viewExpression: (V, Output) -> Unit)
            = OneWayBinding<Input, Output, Converter, V>(this).toView(view, viewExpression)

}

class OneWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayExpression: OneWayExpression<Input, Output, Converter>,
        val binding: Converter = oneWayExpression.binding) : Binding<Input, Output, Converter> {

    var viewExpression: ((V, Output) -> Unit)? = null
    var view: V? = null

    fun convert() = oneWayExpression.expression(binding.convertValue())

    @Suppress("UNCHECKED_CAST")
    fun toView(view: V, viewExpression: ((V, Output) -> Unit)) = apply {
        this.viewExpression = viewExpression
        this.view = view
        notifyValueChange()
        binding.bind(this)
    }

    override fun unbind() {
        binding.unbind(this)
    }

    /**
     * Reruns binding expressions to views.
     */
    override fun notifyValueChange() {
        viewExpression?.let {
            val view = this.view
            if (view != null) {
                it(view, convert())
            }
        }
    }

}


/**
 * Immediately binds the [TextView] to the value of this binding. Subsequent changes are handled by
 * the kind of object it is.
 */
infix fun <Input, TBinding : BindingConverter<Input>, TChar : CharSequence?>
        OneWayExpression<Input, TChar, TBinding>.toText(textView: TextView)
        = toView(textView, TextView::setTextIfNecessary)

infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton)
        = toView(compoundButton, CompoundButton::setCheckedIfNecessary)

infix fun <Input, TBinding : ObservableBindingConverter<Input>>
        OneWayExpression<Input, Calendar, TBinding>.toDatePicker(datePicker: DatePicker)
        = toView(datePicker, DatePicker::setTimeIfNecessary)

