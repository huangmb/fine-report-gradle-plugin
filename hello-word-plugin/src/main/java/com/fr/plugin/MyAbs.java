package com.fr.plugin;

import com.fr.general.FArray;
import com.fr.general.GeneralUtils;
import com.fr.intelli.record.Focus;
import com.fr.intelli.record.Original;
import com.fr.record.analyzer.EnableMetrics;
import com.fr.script.AbstractFunction;
import com.fr.stable.ArrayUtils;
import com.fr.stable.Primitive;

@EnableMetrics
public class MyAbs extends AbstractFunction  {

    @Focus(id = "com.fr.plugin.function.test", text = "Plugin-Test_Function_Abs", source = Original.PLUGIN)
    public Object run(Object[] args) {
        int len = ArrayUtils.getLength(args);
        if (len == 0) {
            return Primitive.ERROR_VALUE;
        } else if (len == 1) {
            return Math.abs(GeneralUtils.objectToNumber(args[0]).doubleValue());
        } else {
            FArray<Double> result = new FArray<Double>();
            for (Object arg : args) {
                result.add(GeneralUtils.objectToNumber(arg).doubleValue());
            }
        }
        return Primitive.ERROR_VALUE;
    }
}
