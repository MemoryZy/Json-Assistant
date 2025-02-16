package cn.memoryzy.json.action.debug;

import com.sun.jdi.*;

import java.util.*;

public class ObjectReferenceToMapConverter {
    // 缓存已访问对象，防止循环引用导致的无限递归
    private final Set<ObjectReference> visitedObjects = new HashSet<>();

    public Map<String, Object> convert(ObjectReference objRef) {
        return convertInternal(objRef, new HashSet<>());
    }

    private Map<String, Object> convertInternal(ObjectReference objRef, Set<ObjectReference> visited) {
        if (objRef == null || visited.contains(objRef)) {
            return Collections.singletonMap("@error", "Cyclic reference or null");
        }
        visited.add(objRef);

        Map<String, Object> result = new LinkedHashMap<>();
        try {
            // 获取对象类型信息
            ReferenceType refType = objRef.referenceType();
            List<Field> fields = refType.allFields(); // 获取所有字段（包括继承的）

            for (Field field : fields) {
                String fieldName = field.name();
                Value fieldValue = objRef.getValue(field);

                // 处理字段值
                Object resolvedValue = resolveValue(fieldValue, new HashSet<>(visited));
                result.put(fieldName, resolvedValue);
            }
        } catch (Exception e) {
            result.put("@error", "Failed to read fields: " + e.getMessage());
        }
        return result;
    }

    private Object resolveValue(Value value, Set<ObjectReference> visited) {
        if (value == null) {
            return null;
        }

        // 处理基本类型
        if (value instanceof PrimitiveValue) {
            return handlePrimitive((PrimitiveValue) value);
        }

        // 处理字符串
        if (value instanceof StringReference) {
            return ((StringReference) value).value();
        }

        // 处理数组
        if (value instanceof ArrayReference) {
            return handleArray((ArrayReference) value, visited);
        }

        // 处理集合（如 List/Set）
        if (isCollection(value)) {
            return handleCollection((ObjectReference) value, visited);
        }

        // 处理其他对象（递归转换）
        if (value instanceof ObjectReference) {
            return handleObject((ObjectReference) value, visited);
        }

        return "Unsupported type: " + value.type().name();
    }

    // 处理基本类型值
    private Object handlePrimitive(PrimitiveValue primitive) {
        if (primitive instanceof BooleanValue) {
            return ((BooleanValue) primitive).value();
        } else if (primitive instanceof IntegerValue) {
            return ((IntegerValue) primitive).value();
        } else if (primitive instanceof LongValue) {
            return ((LongValue) primitive).value();
        } else if (primitive instanceof DoubleValue) {
            return ((DoubleValue) primitive).value();
        } else if (primitive instanceof FloatValue) {
            return ((FloatValue) primitive).value();
        } else if (primitive instanceof CharValue) {
            return ((CharValue) primitive).value();
        } else if (primitive instanceof ShortValue) {
            return ((ShortValue) primitive).value();
        } else if (primitive instanceof ByteValue) {
            return ((ByteValue) primitive).value();
        }
        return "Unknown primitive";
    }

    // 处理数组
    private List<Object> handleArray(ArrayReference arrayRef, Set<ObjectReference> visited) {
        List<Object> list = new ArrayList<>();
        for (Value element : arrayRef.getValues()) {
            list.add(resolveValue(element, visited));
        }
        return list;
    }

    // 处理集合（假设是 java.util.Collection）
    private List<Object> handleCollection(ObjectReference colRef, Set<ObjectReference> visited) {
        try {
            // 调用集合的 toArray() 方法获取内容
            Method toArrayMethod = colRef.referenceType().methodsByName("toArray").get(0);
            // Value arrayValue = colRef.invokeMethod(
            //     // colRef.virtualMachine().threads().get(0),
            //     toArrayMethod,
            //     Collections.emptyList(),
            //     ObjectReference.INVOKE_SINGLE_THREADED
            // );

            // return handleArray((ArrayReference) arrayValue, visited);
            return null;
        } catch (Exception e) {
            return Collections.singletonList("Failed to read collection: " + e.getMessage());
        }
    }

    // 处理普通对象（递归调用）
    private Object handleObject(ObjectReference objRef, Set<ObjectReference> visited) {
        String typeName = objRef.referenceType().name();

        // 处理包装类型（如 Long、Integer、Double 等）
        if (typeName.startsWith("java.lang.")) {
            switch (typeName) {
                case "java.lang.Long":
                    return getPrimitiveFieldValue(objRef, "value");  // 返回 long 值
                case "java.lang.Integer":
                    return getPrimitiveFieldValue(objRef, "value");   // 返回 int 值
                case "java.lang.Double":
                    return getPrimitiveFieldValue(objRef, "value");   // 返回 double 值
                // 添加其他包装类型...
                default:
                    break;
            }
        }

        /*

        private Object handleObject(ObjectReference objRef, Set<ObjectReference> visited) {
    String typeName = objRef.referenceType().name();

    // 处理常用包装类型
    switch (typeName) {
        case "java.lang.Long":
        case "java.lang.Integer":
        case "java.lang.Double":
        case "java.lang.Float":
        case "java.lang.Short":
        case "java.lang.Byte":
        case "java.lang.Character":
        case "java.lang.Boolean":
            return getPrimitiveFieldValue(objRef, "value");
        case "java.lang.String":
            return ((StringReference) objRef).value();
        default:
            // 其他特殊类型（如 Date）或递归处理
            if (typeName.equals("java.util.Date")) {
                return handleDate(objRef); // 自定义处理 Date
            }
            return convertInternal(objRef, visited);
    }
}

         */

        // 其他对象递归处理
        return convertInternal(objRef, visited);
    }

    /**
     * 从对象中提取基本类型字段的值
     */
    private Object getPrimitiveFieldValue(ObjectReference objRef, String fieldName) {
        try {
            Field field = objRef.referenceType().fieldByName(fieldName);
            Value value = objRef.getValue(field);
            if (value instanceof PrimitiveValue) {
                PrimitiveValue primitive = (PrimitiveValue) value;
                // 根据字段类型返回对应值
                switch (primitive.type().name()) {
                    case "long":    return primitive.longValue();
                    case "int":      return primitive.intValue();
                    case "double":   return primitive.doubleValue();
                    case "float":    return primitive.floatValue();
                    case "byte":    return primitive.byteValue();
                    case "short":   return primitive.shortValue();
                    case "char":     return primitive.charValue();
                    case "boolean": return primitive.booleanValue();
                    default:         return "Unsupported primitive";
                }
            }
        } catch (Exception e) {
            return "@error: " + e.getMessage();
        }
        return null;
    }

    private Object handleObject2(ObjectReference objRef, Set<ObjectReference> visited) {
        String typeName = objRef.referenceType().name();

        // 处理包装类型
        if (typeName.equals("java.lang.Long")) {
            return invokePrimitiveMethod(objRef, "longValue");
        } else if (typeName.equals("java.lang.Integer")) {
            return invokePrimitiveMethod(objRef, "intValue");
        } // 其他类型类似...

        // 其他对象递归处理
        return convertInternal(objRef, visited);
    }

    /**
     * 调用对象的取值方法（如 longValue()）
     */
    private Object invokePrimitiveMethod(ObjectReference objRef, String methodName) {
        try {
            // 获取方法并调用
            Method method = objRef.referenceType().methodsByName(methodName).get(0);
            ThreadReference thread = objRef.virtualMachine().allThreads().get(0); // 假设有可用线程
            Value result = objRef.invokeMethod(thread, method, Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);

            if (result instanceof PrimitiveValue) {
                return ((PrimitiveValue) result).longValue(); // 根据方法返回类型调整
            }
        } catch (Exception e) {
            return "@error: " + e.getMessage();
        }
        return null;
    }

    // 判断是否为集合类型
    private boolean isCollection(Value value) {
        if (!(value instanceof ObjectReference)) return false;
        ReferenceType type = ((ObjectReference) value).referenceType();
        return type instanceof ClassType &&
               ((ClassType) type).allInterfaces().stream()
                   .anyMatch(iface -> iface.name().equals("java.util.Collection"));
    }
}