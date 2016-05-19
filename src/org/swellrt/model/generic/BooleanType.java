package org.swellrt.model.generic;


import org.swellrt.model.ReadableBoolean;
import org.swellrt.model.ReadableNumber;
import org.swellrt.model.ReadableTypeVisitor;
import org.waveprotocol.wave.model.adt.ObservableBasicValue;
import org.waveprotocol.wave.model.util.CopyOnWriteSet;
import org.waveprotocol.wave.model.util.Preconditions;
import org.waveprotocol.wave.model.wave.SourcesEvents;

public class BooleanType extends Type implements ReadableBoolean,
    SourcesEvents<BooleanType.Listener> {


  public interface Listener {

    void onValueChanged(boolean oldValue, boolean newValue);

  }

  /**
   * Get an instance of BooleanType. This method is used for deserialization.
   * 
   * @param parent the parent Type instance of this string
   * @param valueIndex the index of the value in the parent's value container
   * @return
   */
  protected static BooleanType deserialize(Type parent, String valueIndex) {
    BooleanType string = new BooleanType();
    string.attach(parent, valueIndex);
    return string;
  }

  public final static String TYPE_NAME = "BooleanType";
  public final static String PREFIX = "b";
  public final static String VALUE_ATTR = "v";


  private ObservableBasicValue<String> observableValue;
  private ObservableBasicValue.Listener<String> observableValueListener;

  private String path;

  private Type parent;
  private int valueRef; // the index of this value in the ValuesContainer
  private Boolean initValue;



  private boolean isAttached;

  private final CopyOnWriteSet<Listener> listeners = CopyOnWriteSet.create();


  protected BooleanType() {

    this.initValue = null;
    this.observableValueListener = new ObservableBasicValue.Listener<String>() {

      @Override
      public void onValueChanged(String oldValue, String newValue) {
        for (Listener l : listeners)
          l.onValueChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
      }
    };
  }


  public BooleanType(boolean initValue) {

    this.initValue = initValue;
    this.observableValueListener = new ObservableBasicValue.Listener<String>() {

      @Override
      public void onValueChanged(String oldValue, String newValue) {
        for (Listener l: listeners)
          l.onValueChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
      }
    };

  }


  @Override
  protected String getPrefix() {
    return PREFIX;
  }

  @Override
  protected void attach(Type parent) {
    Preconditions.checkArgument(parent.hasValuesContainer(),
        "Invalid parent type for a primitive value");
    this.parent = parent;
    observableValue = parent.getValuesContainer().add(Boolean.toString(initValue));
    observableValue.addListener(observableValueListener);
    valueRef = parent.getValuesContainer().indexOf(observableValue);
    isAttached = true;
  }

  @Override
  protected void attach(Type parent, String valueIndex) {
    Preconditions.checkArgument(parent.hasValuesContainer(),
        "Invalid parent type for a primitive value");

    this.parent = parent;

    Integer index = null;
    try {
      index = Integer.valueOf(valueIndex);
    } catch (NumberFormatException e) {

    }

    Preconditions.checkNotNull(index, "Value index is null");

    valueRef = index;
    observableValue = parent.getValuesContainer().get(index);

    if (observableValue == null) {
      // return a non-attached value
      // this singals the actual value hasn't been received yet.
      return;
    }


    observableValue.addListener(observableValueListener);

    isAttached = true;
  }

  protected void deattach() {
    Preconditions.checkArgument(isAttached, "Unable to deattach an unattached BooleanType");
    observableValue.removeListener(this.observableValueListener);
    observableValue = null;
    isAttached = false;
  }


  @Override
  protected boolean isAttached() {
    return isAttached;
  }


  @Override
  protected String serialize() {
    Preconditions.checkArgument(isAttached, "Unable to serialize an unattached BooleanType");
    return PREFIX + "+" + Integer.toString(valueRef);
  }

  @Override
  protected ListElementInitializer getListElementInitializer() {
    return new ListElementInitializer() {

      @Override
      public String getType() {
        return PREFIX;
      }

      @Override
      public String getBackendId() {
        Preconditions.checkArgument(isAttached, "Unable to initialize an unattached BooleanType");
        return serialize();
      }

    };
  }

  //
  // Listeners
  //

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }


  @Override
  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }


  //
  // Number operations
  //

  public boolean getValue() {
    if (!isAttached())
      return initValue;
    else
      return Boolean.valueOf(observableValue.get());
  }


  public void setValue(double value) {
    setValue(Double.toString(value));
  }

  public void setValue(int value) {
    setValue(Integer.toString(value));
  }

  public void setValue(String value) {
    if (isAttached()) {
      if (!value.equals(observableValue.get())) {
        observableValue.set(value);
        parent.markValueUpdate(this);
      }
    }
  }

  @Override
  public String getDocumentId() {
    return null;
  }


  @Override
  public String getType() {
    return TYPE_NAME;
  }


  @Override
  protected void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getPath() {
    if (path == null && parent != null && isAttached) {
      path = parent.getPath() + "." + parent.getValueReference(this);
    }
    return path;
  }

  @Override
  protected boolean hasValuesContainer() {
    return false;
  }

  @Override
  protected ValuesContainer getValuesContainer() {
    return null;
  }

  @Override
  protected String getValueReference(Type value) {
    return null;
  }

  protected Integer getValueRefefence() {
    return valueRef;
  }

  @Override
  public Model getModel() {
    return parent.getModel();
  }

  @Override
  public void accept(ReadableTypeVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public MapType asMap() {
    return null;
  }

  @Override
  public StringType asString() {
    return null;
  }

  @Override
  public ListType asList() {
    return null;
  }

  @Override
  public TextType asText() {
    return null;
  }

  @Override
  public FileType asFile() {
    return null;
  }

  @Override
  public ReadableNumber asNumber() {
    return null;
  }

  @Override
  public ReadableBoolean asBoolean() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj instanceof BooleanType) {
      BooleanType other = (BooleanType) obj;
      // It's suppossed comparasion between types in the same container
      return (other.valueRef == this.valueRef);
    }


    return false;
  }

}
