// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.util;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import com.uncommongoods.tugboat.bridge.data.Country;
import com.uncommongoods.tugboat.bridge.data.State;

/**
 * Shared wiring for the country and state pickers on the address forms.
 *
 * <p>Both combos are editable and match on either the full name or the code, so
 * a user can type "NY" or "New York". Values that are not in the enums are shown
 * in the editor as typed rather than dropped: the carrier is the authority on
 * what is deliverable, not this list.
 */
public final class AddressFormSupport {

    private AddressFormSupport() {}

    /** Populate and configure a country picker. Callers set the initial value. */
    public static void configureCountryCombo(ComboBox<Country> combo) {
        combo.getItems().setAll(Country.values());
        combo.setConverter(new StringConverter<Country>() {
            @Override
            public String toString(Country country) {
                return country == null ? "" : country.getName();
            }

            @Override
            public Country fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return combo.getItems().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(string) || c.getCode().equalsIgnoreCase(string))
                    .findFirst()
                    .orElse(null);
            }
        });
    }

    /** Populate and configure a state picker. Callers set the initial value. */
    public static void configureStateCombo(ComboBox<State> combo) {
        combo.getItems().setAll(State.values());
        combo.setConverter(new StringConverter<State>() {
            @Override
            public String toString(State state) {
                return state == null ? "" : state.getName();
            }

            @Override
            public State fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return combo.getItems().stream()
                    .filter(s -> s.getName().equalsIgnoreCase(string) || s.getAbbreviation().equalsIgnoreCase(string))
                    .findFirst()
                    .orElse(null);
            }
        });
    }

    /**
     * Show a stored ISO country code in the picker, falling back to the raw text
     * when the code is not one we know.
     */
    public static void applyCountryCode(ComboBox<Country> combo, String code) {
        if (code == null) return;

        Country country = findCountryByCode(code);
        if (country != null) {
            combo.setValue(country);
        } else {
            combo.getEditor().setText(code);
            System.err.println("Unknown country code: " + code);
        }
    }

    /**
     * Show a stored state abbreviation in the picker, falling back to the raw
     * text when the abbreviation is not one we know.
     */
    public static void applyStateAbbreviation(ComboBox<State> combo, String abbreviation) {
        if (abbreviation == null) return;

        State state = findStateByAbbreviation(abbreviation);
        if (state != null) {
            combo.setValue(state);
        } else {
            combo.getEditor().setText(abbreviation);
            System.err.println("Unknown state code: " + abbreviation);
        }
    }

    public static Country findCountryByCode(String code) {
        if (code == null) return null;

        for (Country country : Country.values()) {
            if (country.getCode().equalsIgnoreCase(code)) {
                return country;
            }
        }
        return null;
    }

    public static State findStateByAbbreviation(String abbreviation) {
        if (abbreviation == null) return null;

        for (State state : State.values()) {
            if (state.getAbbreviation().equalsIgnoreCase(abbreviation)) {
                return state;
            }
        }
        return null;
    }
}
