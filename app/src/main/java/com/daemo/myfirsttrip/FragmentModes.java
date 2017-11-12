package com.daemo.myfirsttrip;

enum ListFragmentMode {
    /**
     * This fragment has been created to choose an element
     */
    CHOOSE,
    /**
     * This fragment is being shown inside the detail fragment
     */
    NESTED,
    /**
     * This fragment is being shown inside the detail fragment that is in edit mode
     */
    NESTED_EDIT,
    /**
     * This fragment shows all the elements
     */
    ALL
}

enum DetailFragmentMode {
    /**
     * The element shown in this fragment can be edited
     */
    EDIT,
    /**
     * The element shown is new
     */
    NEW,
    /**
     * The element shown in this fragment can not be edited
     */
    VIEW
}
