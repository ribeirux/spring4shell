package org.springframework.beans;

import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class EvalTreePrinter extends BeanWrapperImpl {
    private static final Set<Class<?>> CLASSES_TO_IGNORE = Set.of(String.class);

    private final ConversionService conversionService;

    public EvalTreePrinter(final Object object, final ConversionService conversionService) {
        super(object);
        this.setExtractOldValueForEditor(true);
        this.setAutoGrowNestedPaths(true);
        this.setAutoGrowCollectionLimit(256);
        this.conversionService = conversionService;
    }

    public EvalTreePrinter(
        final Object object,
        final String nestedPath,
        final EvalTreePrinter evalTreePrinter,
        final ConversionService conversionService
    ) {
        super(object, nestedPath, evalTreePrinter);
        this.conversionService = conversionService;
    }

    @Override
    protected BeanWrapperImpl newNestedPropertyAccessor(final Object object, final String nestedPath) {
        return new EvalTreePrinter(object, nestedPath, this, this.conversionService);
    }

    public void print() throws Exception {
        final Method getNestedPropertyAccessor = AbstractNestablePropertyAccessor.class
            .getDeclaredMethod("getNestedPropertyAccessor", String.class);
        getNestedPropertyAccessor.setAccessible(true);

        final Collection<Object> seen = new HashSet<>();
        final Deque<EvalPath> stack = new LinkedList<>();

        stack.addLast(new EvalPath("", this));

        while (!stack.isEmpty()) {
            final EvalPath currentPath = stack.removeFirst();
            if (seen.add(currentPath.wrapper.getWrappedInstance()) && !CLASSES_TO_IGNORE.contains(currentPath.wrapper.getWrappedClass())) {
                final CachedIntrospectionResults results = CachedIntrospectionResults.forClass(currentPath.wrapper.getWrappedClass());
                final Collection<String> writeOperations = new ArrayList<>();
                for (final PropertyDescriptor d : results.getPropertyDescriptors()) {
                    final Method writeMethod = d.getWriteMethod();
                    final Class<?> propertyType = d.getPropertyType();
                    if (writeMethod != null) {
                        writeOperations.add(" - " + writeMethod);
                    } else if (d.getPropertyType().isArray()) {
                        final Class<?> componentType = d.getPropertyType().getComponentType();
                        if (conversionService.canConvert(String.class, componentType)) {
                            writeOperations.add(" - " + d.getReadMethod());
                        }
                    } else if (List.class.isAssignableFrom(propertyType)) {
                        writeOperations.add(" - " + d.getReadMethod());
                    } else if (Map.class.isAssignableFrom(propertyType)) {
                        writeOperations.add(" - " + d.getReadMethod());
                    }

                    final EvalTreePrinter accessor = propertyAccessorOf(getNestedPropertyAccessor, currentPath.wrapper, d.getName());
                    if(accessor != null) {
                        if (currentPath.prefix.isEmpty()) {
                            stack.addLast(new EvalPath(d.getName(), accessor));
                        } else if (accessor.getWrappedClass() != Class.class) {
                            stack.addLast(new EvalPath(currentPath.prefix + "." + d.getName(), accessor));
                        }
                    }
                }

                if (!writeOperations.isEmpty()) {
                    System.out.println(currentPath.prefix);
                    for (final String writeOperation : writeOperations) {
                        System.out.println(writeOperation);
                    }
                }
            }
        }
    }

    @Nullable
    private EvalTreePrinter propertyAccessorOf(
        final Method getNestedPropertyAccessor,
        final EvalTreePrinter evalTree,
        final String name
    ) {
        try {
            return (EvalTreePrinter) getNestedPropertyAccessor.invoke(evalTree, name);
        } catch (final Exception e) {
            return null;
        }
    }

    static class EvalPath {
        String prefix;
        EvalTreePrinter wrapper;
        public EvalPath(final String prefix, final EvalTreePrinter wrapper) {
            this.prefix = prefix;
            this.wrapper = wrapper;
        }
    }
}
