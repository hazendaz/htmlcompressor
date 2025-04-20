/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jargs.gnu;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Largely GNU-compatible command-line options parser. Has short (-v) and long-form (--verbose) option support, and also
 * allows options with associated values (-d 2, --debug 2, --debug=2). Option processing can be explicitly terminated by
 * the argument '--'.
 *
 * @author Steve Purcell
 *
 * @version $Revision$
 *
 * @see <a href="https://github.com/purcell/jargs/tree/master/src/examples/java/com/sanityinc/jargs/examples">jargs
 *      examples</a>
 */
public class CmdLineParser {

    /** The remaining args. */
    private List<String> remainingArgs;

    /** The options. */
    private HashMap<Object, Option> options = new HashMap<>(10);

    /** The values. */
    private HashMap<String, Object> values = new HashMap<>(10);

    /**
     * Base class for exceptions that may be thrown when options are parsed.
     */
    public abstract static class OptionException extends Exception {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new option exception.
         *
         * @param msg
         *            the msg
         */
        OptionException(String msg) {
            super(msg);
        }
    }

    /**
     * Thrown when the parsed command-line contains an option that is not recognised. <code>getMessage()</code> returns
     * an error string suitable for reporting the error to the user (in English).
     */
    public static class UnknownOptionException extends OptionException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The option name. */
        private final String optionName;

        /**
         * Instantiates a new unknown option exception.
         *
         * @param optionName
         *            the option name
         */
        UnknownOptionException(String optionName) {
            this(optionName, "Unknown option '" + optionName + "'");
        }

        /**
         * Instantiates a new unknown option exception.
         *
         * @param optionName
         *            the option name
         * @param msg
         *            the msg
         */
        UnknownOptionException(String optionName, String msg) {
            super(msg);
            this.optionName = optionName;
        }

        /**
         * Gets the option name.
         *
         * @return the name of the option that was unknown (e.g. "-u")
         */
        public String getOptionName() {
            return this.optionName;
        }
    }

    /**
     * Thrown when the parsed commandline contains multiple concatenated short options, such as -abcd, where one is
     * unknown. <code>getMessage()</code> returns an english human-readable error string.
     *
     * @author Vidar Holen
     */
    public static class UnknownSuboptionException extends UnknownOptionException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The suboption. */
        private final char suboption;

        /**
         * Instantiates a new unknown suboption exception.
         *
         * @param option
         *            the option
         * @param suboption
         *            the suboption
         */
        UnknownSuboptionException(String option, char suboption) {
            super(option, "Illegal option: '" + suboption + "' in '" + option + "'");
            this.suboption = suboption;
        }

        /**
         * Gets the suboption.
         *
         * @return the suboption
         */
        public char getSuboption() {
            return suboption;
        }
    }

    /**
     * Thrown when the parsed commandline contains multiple concatenated short options, such as -abcd, where one or more
     * requires a value. <code>getMessage()</code> returns an english human-readable error string.
     *
     * @author Vidar Holen
     */
    public static class NotFlagException extends UnknownOptionException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The notflag. */
        private final char notflag;

        /**
         * Instantiates a new not flag exception.
         *
         * @param option
         *            the option
         * @param unflaggish
         *            the unflaggish
         */
        NotFlagException(String option, char unflaggish) {
            super(option, "Illegal option: '" + option + "', '" + unflaggish + "' requires a value");
            notflag = unflaggish;
        }

        /**
         * Gets the option char.
         *
         * @return the first character which wasn't a boolean (e.g 'c')
         */
        public char getOptionChar() {
            return notflag;
        }
    }

    /**
     * Thrown when an illegal or missing value is given by the user for an option that takes a value.
     * <code>getMessage()</code> returns an error string suitable for reporting the error to the user (in English).
     */
    public static class IllegalOptionValueException extends OptionException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The option. */
        private final Option option;

        /** The value. */
        private final String value;

        /**
         * Instantiates a new illegal option value exception.
         *
         * @param opt
         *            the opt
         * @param value
         *            the value
         */
        public IllegalOptionValueException(Option opt, String value) {
            super("Illegal value '" + value + "' for option "
                    + (opt.shortForm() != null ? "-" + opt.shortForm() + "/" : "") + "--" + opt.longForm());
            this.option = opt;
            this.value = value;
        }

        /**
         * Gets the option.
         *
         * @return the name of the option whose value was illegal (e.g. "-u")
         */
        public Option getOption() {
            return this.option;
        }

        /**
         * Gets the value.
         *
         * @return the illegal value
         */
        public String getValue() {
            return this.value;
        }

    }

    /**
     * Representation of a command-line option.
     */
    public abstract static class Option implements Serializable {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The short form. */
        private String shortForm;

        /** The long form. */
        private String longForm;

        /** The wants value. */
        private boolean wantsValue;

        /**
         * Instantiates a new option.
         *
         * @param longForm
         *            the long form
         * @param wantsValue
         *            the wants value
         */
        protected Option(String longForm, boolean wantsValue) {
            this(null, longForm, wantsValue);
        }

        /**
         * Instantiates a new option.
         *
         * @param shortForm
         *            the short form
         * @param longForm
         *            the long form
         * @param wantsValue
         *            the wants value
         */
        protected Option(char shortForm, String longForm, boolean wantsValue) {
            this(new String(new char[] { shortForm }), longForm, wantsValue);
        }

        /**
         * Instantiates a new option.
         *
         * @param shortForm
         *            the short form
         * @param longForm
         *            the long form
         * @param wantsValue
         *            the wants value
         */
        private Option(String shortForm, String longForm, boolean wantsValue) {
            if (longForm == null)
                throw new IllegalArgumentException("Null longForm not allowed");
            this.shortForm = shortForm;
            this.longForm = longForm;
            this.wantsValue = wantsValue;
        }

        /**
         * Short form.
         *
         * @return the string
         */
        public String shortForm() {
            return this.shortForm;
        }

        /**
         * Long form.
         *
         * @return the string
         */
        public String longForm() {
            return this.longForm;
        }

        /**
         * Tells whether or not this option wants a value.
         *
         * @return true, if successful
         */
        public boolean wantsValue() {
            return this.wantsValue;
        }

        /**
         * Gets the value.
         *
         * @param arg
         *            the arg
         * @param locale
         *            the locale
         *
         * @return the value
         *
         * @throws IllegalOptionValueException
         *             the illegal option value exception
         */
        public final Object getValue(String arg, Locale locale) throws IllegalOptionValueException {
            if (this.wantsValue) {
                if (arg == null) {
                    throw new IllegalOptionValueException(this, "");
                }
                return this.parseValue(arg, locale);
            } else {
                return Boolean.TRUE;
            }
        }

        /**
         * Override to extract and convert an option value passed on the command-line.
         *
         * @param arg
         *            the arg
         * @param locale
         *            the locale
         *
         * @return the object
         *
         * @throws IllegalOptionValueException
         *             the illegal option value exception
         */
        protected Object parseValue(String arg, Locale locale) throws IllegalOptionValueException {
            return null;
        }

        /**
         * The Class BooleanOption.
         */
        public static class BooleanOption extends Option {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /**
             * Instantiates a new boolean option.
             *
             * @param shortForm
             *            the short form
             * @param longForm
             *            the long form
             */
            public BooleanOption(char shortForm, String longForm) {
                super(shortForm, longForm, false);
            }

            /**
             * Instantiates a new boolean option.
             *
             * @param longForm
             *            the long form
             */
            public BooleanOption(String longForm) {
                super(longForm, false);
            }
        }

        /**
         * An option that expects an integer value.
         */
        public static class IntegerOption extends Option {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /**
             * Instantiates a new integer option.
             *
             * @param shortForm
             *            the short form
             * @param longForm
             *            the long form
             */
            public IntegerOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            /**
             * Instantiates a new integer option.
             *
             * @param longForm
             *            the long form
             */
            public IntegerOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Object parseValue(String arg, Locale locale) throws IllegalOptionValueException {
                try {
                    return Integer.valueOf(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a long integer value.
         */
        public static class LongOption extends Option {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /**
             * Instantiates a new long option.
             *
             * @param shortForm
             *            the short form
             * @param longForm
             *            the long form
             */
            public LongOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            /**
             * Instantiates a new long option.
             *
             * @param longForm
             *            the long form
             */
            public LongOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Object parseValue(String arg, Locale locale) throws IllegalOptionValueException {
                try {
                    return Long.valueOf(arg);
                } catch (NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a floating-point value.
         */
        public static class DoubleOption extends Option {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /**
             * Instantiates a new double option.
             *
             * @param shortForm
             *            the short form
             * @param longForm
             *            the long form
             */
            public DoubleOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            /**
             * Instantiates a new double option.
             *
             * @param longForm
             *            the long form
             */
            public DoubleOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Object parseValue(String arg, Locale locale) throws IllegalOptionValueException {
                try {
                    NumberFormat format = NumberFormat.getNumberInstance(locale);
                    Number num = format.parse(arg);
                    return num.doubleValue();
                } catch (ParseException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a string value.
         */
        public static class StringOption extends Option {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /**
             * Instantiates a new string option.
             *
             * @param shortForm
             *            the short form
             * @param longForm
             *            the long form
             */
            public StringOption(char shortForm, String longForm) {
                super(shortForm, longForm, true);
            }

            /**
             * Instantiates a new string option.
             *
             * @param longForm
             *            the long form
             */
            public StringOption(String longForm) {
                super(longForm, true);
            }

            @Override
            protected Object parseValue(String arg, Locale locale) {
                return arg;
            }
        }
    }

    /**
     * Add the specified Option to the list of accepted options.
     *
     * @param opt
     *            the opt
     *
     * @return the option
     */
    public final Option addOption(Option opt) {
        if (opt.shortForm() != null) {
            this.options.put("-" + opt.shortForm(), opt);
        }
        this.options.put("--" + opt.longForm(), opt);
        return opt;
    }

    /**
     * Convenience method for adding a string option.
     *
     * @param shortForm
     *            the short form
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addStringOption(char shortForm, String longForm) {
        return addOption(new Option.StringOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a string option.
     *
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addStringOption(String longForm) {
        return addOption(new Option.StringOption(longForm));
    }

    /**
     * Convenience method for adding an integer option.
     *
     * @param shortForm
     *            the short form
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addIntegerOption(char shortForm, String longForm) {
        return addOption(new Option.IntegerOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding an integer option.
     *
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addIntegerOption(String longForm) {
        return addOption(new Option.IntegerOption(longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     *
     * @param shortForm
     *            the short form
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addLongOption(char shortForm, String longForm) {
        return addOption(new Option.LongOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     *
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addLongOption(String longForm) {
        return addOption(new Option.LongOption(longForm));
    }

    /**
     * Convenience method for adding a double option.
     *
     * @param shortForm
     *            the short form
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addDoubleOption(char shortForm, String longForm) {
        return addOption(new Option.DoubleOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a double option.
     *
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addDoubleOption(String longForm) {
        return addOption(new Option.DoubleOption(longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     *
     * @param shortForm
     *            the short form
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addBooleanOption(char shortForm, String longForm) {
        return addOption(new Option.BooleanOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     *
     * @param longForm
     *            the long form
     *
     * @return the new Option
     */
    public final Option addBooleanOption(String longForm) {
        return addOption(new Option.BooleanOption(longForm));
    }

    /**
     * Equivalent to {@link #getOptionValue(Option, Object) getOptionValue(o, null)}.
     *
     * @param o
     *            the o
     *
     * @return the option value
     */
    public final Object getOptionValue(Option o) {
        return getOptionValue(o, null);
    }

    /**
     * Gets the option value.
     *
     * @param o
     *            the o
     * @param def
     *            the def
     *
     * @return the parsed value of the given Option, or the given default 'def' if the option was not set
     */
    @SuppressWarnings("unchecked")
    public final Object getOptionValue(Option o, Object def) {
        List<String> v = (ArrayList<String>) values.get(o.longForm());

        if (v == null) {
            return def;
        } else if (v.isEmpty()) {
            return null;
        } else {
            Object result = v.get(0);
            v.remove(0);
            return result;
        }
    }

    /**
     * Gets the option values.
     *
     * @param option
     *            the option
     *
     * @return A Vector giving the parsed values of all the occurrences of the given Option, or an empty Vector if the
     *         option was not set.
     */
    public final List<String> getOptionValues(Option option) {
        List<String> result = new ArrayList<>();

        while (true) {
            Object o = getOptionValue(option, null);

            if (o == null) {
                return result;
            } else {
                result.add((String) o);
            }
        }
    }

    /**
     * Gets the remaining args.
     *
     * @return the non-option arguments
     */
    public final List<String> getRemainingArgs() {
        return this.remainingArgs;
    }

    /**
     * Extract the options and non-option arguments from the given list of command-line arguments. The default locale is
     * used for parsing options whose values might be locale-specific.
     *
     * @param argv
     *            the argv
     *
     * @throws IllegalOptionValueException
     *             the illegal option value exception
     * @throws UnknownOptionException
     *             the unknown option exception
     */
    public final void parse(String[] argv) throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        parse(argv, Locale.getDefault());
    }

    /**
     * Extract the options and non-option arguments from the given list of command-line arguments. The specified locale
     * is used for parsing options whose values might be locale-specific.
     *
     * @param argv
     *            the argv
     * @param locale
     *            the locale
     *
     * @throws IllegalOptionValueException
     *             the illegal option value exception
     * @throws UnknownOptionException
     *             the unknown option exception
     */
    public final void parse(String[] argv, Locale locale) throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        List<String> otherArgs = new ArrayList<>();
        int position = 0;
        this.values = new HashMap<>(10);
        while (position < argv.length) {
            String curArg = argv[position];
            if (curArg.startsWith("-")) {
                if ("--".equals(curArg)) {
                    // end of options
                    position += 1;
                    break;
                }
                String valueArg = null;
                if (curArg.startsWith("--")) {
                    // handle --arg=value
                    int equalsPos = curArg.indexOf('=');
                    if (equalsPos != -1) {
                        valueArg = curArg.substring(equalsPos + 1);
                        curArg = curArg.substring(0, equalsPos);
                    }
                } else if (curArg.length() > 2) {
                    // handle -abcd
                    for (int i = 1; i < curArg.length(); i++) {
                        Option opt = this.options.get("-" + curArg.charAt(i));
                        if (opt == null)
                            throw new UnknownSuboptionException(curArg, curArg.charAt(i));
                        if (opt.wantsValue())
                            throw new NotFlagException(curArg, curArg.charAt(i));
                        addValue(opt, opt.getValue(null, locale));

                    }
                    position++;
                    continue;
                }

                Option opt = this.options.get(curArg);
                if (opt == null) {
                    throw new UnknownOptionException(curArg);
                }
                Object value;
                if (opt.wantsValue()) {
                    if (valueArg == null) {
                        position += 1;
                        if (position < argv.length) {
                            valueArg = argv[position];
                        }
                    }
                    value = opt.getValue(valueArg, locale);
                } else {
                    value = opt.getValue(null, locale);
                }

                addValue(opt, value);

                position += 1;
            } else {
                otherArgs.add(curArg);
                position += 1;
            }
        }
        for (; position < argv.length; ++position) {
            otherArgs.add(argv[position]);
        }

        this.remainingArgs = new ArrayList<>(otherArgs.size());
        otherArgs.addAll(remainingArgs);
    }

    /**
     * Adds the value.
     *
     * @param opt
     *            the opt
     * @param value
     *            the value
     */
    @SuppressWarnings("unchecked")
    private void addValue(Option opt, Object value) {
        String lf = opt.longForm();

        List<Object> v = (ArrayList<Object>) values.get(lf);

        if (v == null) {
            v = new ArrayList<>();
            values.put(lf, v);
        }

        v.add(value);
    }

}
