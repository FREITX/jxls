# Jxls Reader

Jxls provides [jxls-reader](https://bitbucket.org/leonate/jxls-reader/) module to read XLS files and populate Java beans with spreadsheet data.
XML configuration is used to indicate how exactly an input Excel file should be parsed and how to populate the data.

After the release of Jxls-2 the existing [Jxls 1.x Reader](http://jxls.sourceforge.net/1.x/reference/reader.html) module was migrated to [Bitbucket](https://bitbucket.org/leonate/jxls-reader/).

During the migration the following changes were made  

1. The main java package was renamed from `net.sf.jxls.reader` to `org.jxls.reader`
2. Dependency versions of  [Apache POI](https://poi.apache.org/) and [Apache Commons](https://commons.apache.org/) were updated to the latest version
3. [Slf4j](http://www.slf4j.org/) with [Logback](http://logback.qos.ch/) is now used for logging instead of Log4j and commons-logging 
4. The artifact version was bumped to 2.0.0

## Maven dependency

The latest Jxls Reader module was released to Central Maven repository and to use it you should add the following dependency in your pom.xml

```
<dependency>
    <groupId>org.jxls.fork</groupId>
    <artifactId>jxls-reader</artifactId>
    <version>TODO-var jxlsReaderVersion</version>
</dependency> 
```

## Construction of XLSReader using XML config file

To use Jxls-Reader to parse excel files and populate your Java objects with read data you have to construct *XLSReader* object first. 
The easiest way to do this is to use a special XML configuration file. 

Let's use departmentdata.xls Excel file with some department data to demonstrate this method

The mapping between spreadsheet cells and Java objects is configured in XML file. 
Mapping file structure is rather straightforward. 
Let's take a look at xml mapping file for *Sheet1* of our departmentdata.xls sample file

```
        <?xml version="1.0" encoding="ISO-8859-1"?>
        <workbook>
            <worksheet name="Sheet1">
                <section startRow="0" endRow="6">
                    <mapping cell="B1">department.name</mapping>
                    <mapping cell="A4">department.chief.name</mapping>
                    <mapping cell="B4">department.chief.age</mapping>
                    <mapping cell="D4">department.chief.payment</mapping>
                    <mapping row="3" col="4">department.chief.bonus</mapping>
                </section>
                <loop startRow="7" endRow="7" items="department.staff" var="employee" varType="org.jxls.reader.sample.Employee">
                    <section startRow="7" endRow="7">
                        <mapping row="7" col="0">employee.name</mapping>
                        <mapping row="7" col="1">employee.age</mapping>
                        <mapping row="7" col="3">employee.payment</mapping>
                        <mapping row="7" col="4">employee.bonus</mapping>
                    </section>
                    <loopbreakcondition>
                        <rowcheck offset="0">
                            <cellcheck offset="0">Employee Payment Totals:</cellcheck>
                        </rowcheck>
                    </loopbreakcondition>
                </loop>
            </worksheet>
        </workbook>
```

As we can see the root element of xml file is `workbook` and it can contain any number of child `worksheet` elements. 
`worksheet` tag should contain name attribute indicating the name of excel worksheet which it describes ( *Sheet1* in our case).

`worksheet` element can contain any number of section and loop child elements.

`section` element represents a simple block of spreadsheet cells. The first and the last rows of the block are specified with `startRow` and `endRow` attributes

In the current version you have to specify sections for the whole excel sheet so that it is completely broken down into sections. 
It means that even if you are not going to read for example the first rows of the sheet you need to create an empty section 
(a section without mappings but with startRow/endRow attributes) so that these rows are reflected in XML file. 
The unnecessary rows will be skipped and all other sections will be read as required.
Mapping of XLS cells onto Java bean properties is defined using `mapping` tag which looks like following

```
<mapping cell="B1">department.name</mapping>
```
          
You also can use cell attribute to specify mapped cell and the body of the tag for a full property name to populate from this cell. By full property name we mean bean name concatenated with property name like department.name or department.chief.payment . 
Another option to specify mapped cell is to use cell row and column numbers (zero-based)

```
<mapping row="3" col="4">department.chief.bonus</mapping>
```

This defines mapping for E4 cell and maps it to department.chief.bonus property.

loop element defines loop (repetitive) block of excel rows. It should contain `startRow` and `endRow` attributes to specify start and end row of this repetitive block. 
`items` attribute names collection which should be populated with loop block data as it is known in our bean context map. 
`var` attribute specifies how to refer to each collection item during iteration in the inner sections. 
`varType` attribute defines full Java class name for collection item.

```
<loop startRow="7" endRow="7" items="department.staff" var="employee" varType="org.jxls.reader.sample.Employee">
```
             
`loop` element can contain any number of inner section and loop elements and have to contain `loopbreakcondition` definition. 
This describes break condition to stop loop iteration. 
In our sample it is as simple as specifying that next row after employees data must contain "Employee Payment Totals:" string in the first cell

```
<loopbreakcondition>
    <rowcheck offset="0">
        <cellcheck offset="0">Employee Payment Totals:</cellcheck>
    </rowcheck>
</loopbreakcondition>
```
                
This is all you need to know to create XML mapping configuration file. 
Next is a simple sample of code which uses *ReaderBuilder* class to apply XML mapping file to departmentdata.xls
to construct *XLSReader* class and read XLS data populating corresponding Java beans with XLS data

```
InputStream inputXML = new BufferedInputStream(getClass().getResourceAsStream(xmlConfig));
XLSReader mainReader = ReaderBuilder.buildFromXML( inputXML );
InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
Department department = new Department();
Department hrDepartment = new Department();
List departments = new ArrayList();
Map beans = new HashMap();
beans.put("department", department);
beans.put("hrDepartment", hrDepartment);
beans.put("departments", departments);
XLSReadStatus readStatus = mainReader.read( inputXLS, beans);
```
                
## Sheet mapping by index

Jxls Reader supports mapping of sheets by index. 
This can be convenient in case you do not know the names of the sheets. 
In this case the mapping file will look like this

```
        <?xml version="1.0" encoding="ISO-8859-1"?>
        <workbook>
            <worksheet idx="0">
                <section startRow="0" endRow="6">
                <mapping cell="B1">department.name</mapping>
                <mapping cell="A4">department.chief.name</mapping>
                <mapping cell="B4">department.chief.age</mapping>
                <mapping cell="D4">department.chief.payment</mapping>
                <mapping row="3" col="4">department.chief.bonus</mapping>
                </section>
                <loop startRow="7" endRow="7" items="department.staff" var="employee" varType="org.jxls.reader.sample.Employee">
                    <section startRow="7" endRow="7">
                    <mapping row="7" col="0">employee.name</mapping>
                    <mapping row="7" col="1">employee.age</mapping>
                    <mapping row="7" col="3">employee.payment</mapping>
                    <mapping row="7" col="4">employee.bonus</mapping>
                    </section>
                    <loopbreakcondition>
                        <rowcheck offset="0">
                            <cellcheck offset="0">Employee Payment Totals:</cellcheck>
                        </rowcheck>
                    </loopbreakcondition>
                </loop>
            </worksheet>
        </workbook>
```
            
So instead of using name attribute for `worksheet` tag we now can use `idx` attribute to specify sheet index. 
The sheet index is zero based.

## Error processing

By default jXLS throws *XLSDataReadException* if it fails to read some cell value and stops further processing.

You can override this behaviour and allow to skip errors and continue processing with `setSkipErrors(true)` method of *ReaderConfig* class. 
You get an instance of *ReaderConfig* class using *ReaderConfig.getInstance()* method.

```
ReaderConfig.getInstance().setSkipErrors( true );
```
            
All the error messages are stored in *XLSReadStatus* object which is returned from read method. *XLSDataReadException* also contains a reference to this object. 
One can analyse *XLSReadMessage* objects after getting them from *XLSReadStatus* using *getReadMessages()* method.

## Conversion Mechanism

Jxls Reader integrates with [Apache Commons Beanutils](http://commons.apache.org/proper/commons-beanutils/) conversion utilities to perform actual conversion from Excel cell values into bean properties. 
See more about this in [ConvertUtils class](http://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/ConvertUtils.html). 
Jxls Reader uses standard converters for primitive types provided by [org.apache.commons.beanutils.converters](https://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/converters/package-summary.html) package. 
For [java.util.Date](https://docs.oracle.com/javase/7/docs/api/java/util/Date.html) type Jxls Reader provides a custom *DateConverter* 
which uses [Apache POI](https://poi.apache.org/) utility methods to convert from Excel date representation into Date class.

*BeanUtils Converters* for primitive types return a default value when a conversion error occurs. 
Jxls Reader overrides this behaviour in *ReaderConfig* class registering these classes to throw a *ConversionException*. 
One can use `setUseDefaultValuesForPrimitiveTypes(true)` method of *ReaderConfig* class if there is a preference to use a default value. 
For example

```
ReaderConfig.getInstance().setUseDefaultValuesForPrimitiveTypes( true );
```

To define custom converters from an excel cell into a custom property type one should implement *Converter* interface and register the converter with [ConvertUtils class](http://commons.apache.org/proper/commons-beanutils/apidocs/org/apache/commons/beanutils/ConvertUtils.html).

## Javadoc

See [Reader API](https://jxls.sourceforge.net/javadoc/jxls-reader/index.html).
