package dev.iris.tAB.ping;

import dev.iris.tAB.TAB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class FakePingPacketManager {

    private static final String HANDLER_PREFIX = "tab_fake_ping_bars_";
    private static final String PACKET_CLASS_NAME = "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket";
    private static final String ENTRY_CLASS_NAME = PACKET_CLASS_NAME + "$Entry";
    private static final String ACTION_CLASS_NAME = PACKET_CLASS_NAME + "$Action";

    private final TAB plugin;
    private final PacketRewriter packetRewriter = new PacketRewriter();

    public FakePingPacketManager(TAB plugin) {
        this.plugin = plugin;
    }

    public void inject(Player player) {
        Object channel = getChannel(player);

        if (channel == null) {
            plugin.getLogger().warning("Could not inject fake ping bars packet handler for " + player.getName());
            return;
        }

        String handlerName = handlerName(player);
        executeOnEventLoop(channel, () -> {
            try {
                Object pipeline = invoke(channel, "pipeline");

                if (pipelineGet(pipeline, handlerName) != null) {
                    return;
                }

                Object handler = createOutboundHandler(packetRewriter);
                if (pipelineGet(pipeline, "packet_handler") != null) {
                    invoke(pipeline, "addBefore", "packet_handler", handlerName, handler);
                } else {
                    invoke(pipeline, "addLast", handlerName, handler);
                }
            } catch (ReflectiveOperationException | LinkageError exception) {
                plugin.getLogger().warning("Could not inject fake ping bars packet handler: " + exception.getMessage());
            }
        });
    }

    public void uninject(Player player) {
        Object channel = getChannel(player);

        if (channel == null) {
            return;
        }

        String handlerName = handlerName(player);
        executeOnEventLoop(channel, () -> {
            try {
                Object pipeline = invoke(channel, "pipeline");

                if (pipelineGet(pipeline, handlerName) != null) {
                    invoke(pipeline, "remove", handlerName);
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        });
    }

    public void injectOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            inject(player);
        }
    }

    public void uninjectOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            uninject(player);
        }
    }

    public void refreshOnlinePlayers() {
        injectOnlinePlayers();
        sendLatencyUpdate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void sendLatencyUpdate() {
        try {
            Object updateLatencyAction = getUpdateLatencyAction();
            EnumSet<?> actions = EnumSet.of((Enum) updateLatencyAction);
            List<Object> handles = new ArrayList<>();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Object handle = getHandle(onlinePlayer);
                if (handle != null) {
                    handles.add(handle);
                }
            }

            if (handles.isEmpty()) {
                return;
            }

            Object packet = packetRewriter.rewrite(createPlayerInfoPacket(actions, handles));

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                sendPacket(viewer, packet);
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            plugin.getLogger().warning("Could not send fake ping bars latency update: " + exception.getMessage());
        }
    }

    private String handlerName(Player player) {
        return HANDLER_PREFIX + player.getUniqueId().toString().replace("-", "");
    }

    private Object getChannel(Player player) {
        try {
            Object connection = getConnection(player);
            Object networkConnection = getFieldValue(connection, "connection");
            Object channel = getFieldValue(networkConnection, "channel");

            return channel;
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }

        return null;
    }

    private void sendPacket(Player player, Object packet) throws ReflectiveOperationException {
        Object connection = getConnection(player);
        Method send = findMethod(connection.getClass(), "send", Class.forName("net.minecraft.network.protocol.Packet"));
        send.invoke(connection, packet);
    }

    private Object getConnection(Player player) throws ReflectiveOperationException {
        Object handle = getHandle(player);
        return getFieldValue(handle, "connection");
    }

    private Object getHandle(Player player) throws ReflectiveOperationException {
        Method getHandle = player.getClass().getMethod("getHandle");
        return getHandle.invoke(player);
    }

    @SuppressWarnings("unchecked")
    private Object getUpdateLatencyAction() throws ReflectiveOperationException {
        Class<?> actionClass = Class.forName(ACTION_CLASS_NAME);
        return Enum.valueOf((Class<Enum>) actionClass.asSubclass(Enum.class), "UPDATE_LATENCY");
    }

    private Object createPlayerInfoPacket(EnumSet<?> actions, Collection<?> handles) throws ReflectiveOperationException {
        Class<?> packetClass = Class.forName(PACKET_CLASS_NAME);
        Constructor<?> constructor = packetClass.getConstructor(EnumSet.class, Collection.class);
        return constructor.newInstance(actions, handles);
    }

    private void executeOnEventLoop(Object channel, Runnable runnable) {
        try {
            Object eventLoop = invoke(channel, "eventLoop");
            invoke(eventLoop, "execute", runnable);
        } catch (ReflectiveOperationException | LinkageError exception) {
            plugin.getLogger().warning("Could not access Netty event loop for fake ping bars: " + exception.getMessage());
        }
    }

    private Object createOutboundHandler(PacketRewriter packetRewriter) throws ReflectiveOperationException {
        Class<?> outboundHandlerClass = Class.forName("io.netty.channel.ChannelOutboundHandler");
        ClassLoader classLoader = outboundHandlerClass.getClassLoader();

        return Proxy.newProxyInstance(
                classLoader,
                new Class<?>[] {outboundHandlerClass},
                (proxy, method, args) -> handleOutboundMethod(packetRewriter, method, args)
        );
    }

    private Object handleOutboundMethod(PacketRewriter packetRewriter, Method method, Object[] args) throws ReflectiveOperationException {
        String methodName = method.getName();

        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(methodName);
        }

        if (methodName.equals("write") && args != null && args.length == 3) {
            Object context = args[0];
            Object message = packetRewriter.rewrite(args[1]);
            Object promise = args[2];
            return invoke(context, "write", message, promise);
        }

        if (methodName.equals("exceptionCaught") && args != null && args.length == 2) {
            return invoke(args[0], "fireExceptionCaught", args[1]);
        }

        if (methodName.equals("handlerAdded") || methodName.equals("handlerRemoved")) {
            return null;
        }

        if (args != null && args.length > 0) {
            return invoke(args[0], methodName, dropFirst(args));
        }

        return null;
    }

    private Object handleObjectMethod(String methodName) {
        return switch (methodName) {
            case "toString" -> "TAB fake ping bars outbound handler";
            case "hashCode" -> System.identityHashCode(this);
            case "equals" -> false;
            default -> null;
        };
    }

    private static Object pipelineGet(Object pipeline, String handlerName) throws ReflectiveOperationException {
        return invoke(pipeline, "get", handlerName);
    }

    private static Object[] dropFirst(Object[] args) {
        Object[] dropped = new Object[args.length - 1];
        System.arraycopy(args, 1, dropped, 0, dropped.length);
        return dropped;
    }

    private static Object invoke(Object source, String methodName, Object... args) throws ReflectiveOperationException {
        Method method = findCompatibleMethod(source.getClass(), methodName, args);
        method.setAccessible(true);
        return method.invoke(source, args);
    }

    private static Method findCompatibleMethod(Class<?> type, String methodName, Object[] args) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && parametersMatch(method.getParameterTypes(), args)) {
                return method;
            }
        }

        Class<?> current = type;

        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && parametersMatch(method.getParameterTypes(), args)) {
                    return method;
                }
            }

            current = current.getSuperclass();
        }

        throw new NoSuchMethodException(methodName);
    }

    private static boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return false;
        }

        for (int index = 0; index < parameterTypes.length; index++) {
            if (args[index] != null && !wrap(parameterTypes[index]).isInstance(args[index])) {
                return false;
            }
        }

        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }

        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }

        return Void.class;
    }

    private static Object getFieldValue(Object source, String fieldName) throws ReflectiveOperationException {
        Field field = findField(source.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(source);
    }

    private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private static Method findMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> current = type;

        while (current != null) {
            try {
                return current.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchMethodException(methodName);
    }

    private final class PacketRewriter {

        private Class<?> packetClass;
        private Class<?> entryClass;
        private Method actionsMethod;
        private Method entriesMethod;
        private Constructor<?> packetConstructor;
        private Constructor<?> entryConstructor;
        private RecordComponent[] entryComponents;

        Object rewrite(Object packet) {
            try {
                if (!isPlayerInfoPacket(packet)) {
                    return packet;
                }

                EnumSet<?> actions = cloneActions(packet);
                if (!containsAction(actions, "UPDATE_LATENCY") && !containsAction(actions, "ADD_PLAYER")) {
                    return packet;
                }

                List<?> entries = (List<?>) entriesMethod.invoke(packet);
                List<Object> rewrittenEntries = new ArrayList<>(entries.size());
                int fakeLatency = plugin.getConfigManager().getFakePingLatency();
                boolean changed = false;

                for (Object entry : entries) {
                    Object rewrittenEntry = rewriteEntry(entry, fakeLatency);
                    rewrittenEntries.add(rewrittenEntry);
                    changed |= rewrittenEntry != entry;
                }

                if (!changed) {
                    return packet;
                }

                return packetConstructor.newInstance(actions, rewrittenEntries);
            } catch (ReflectiveOperationException | LinkageError exception) {
                plugin.getLogger().warning("Could not rewrite fake ping bars packet: " + exception.getMessage());
                return packet;
            }
        }

        private boolean isPlayerInfoPacket(Object packet) throws ReflectiveOperationException {
            ensureInitialized();
            return packetClass.isInstance(packet);
        }

        private void ensureInitialized() throws ReflectiveOperationException {
            if (packetClass != null) {
                return;
            }

            packetClass = Class.forName(PACKET_CLASS_NAME);
            entryClass = Class.forName(ENTRY_CLASS_NAME);
            actionsMethod = packetClass.getMethod("actions");
            entriesMethod = packetClass.getMethod("entries");
            packetConstructor = packetClass.getConstructor(EnumSet.class, List.class);
            entryComponents = entryClass.getRecordComponents();
            Class<?>[] parameterTypes = new Class<?>[entryComponents.length];

            for (int index = 0; index < entryComponents.length; index++) {
                parameterTypes[index] = entryComponents[index].getType();
            }

            entryConstructor = entryClass.getConstructor(parameterTypes);
        }

        private EnumSet<?> cloneActions(Object packet) throws ReflectiveOperationException {
            EnumSet<?> actions = (EnumSet<?>) actionsMethod.invoke(packet);
            return actions.clone();
        }

        private boolean containsAction(EnumSet<?> actions, String actionName) {
            for (Object action : actions) {
                if (action instanceof Enum<?> enumAction && Objects.equals(enumAction.name(), actionName)) {
                    return true;
                }
            }

            return false;
        }

        private Object rewriteEntry(Object entry, int fakeLatency) throws ReflectiveOperationException {
            Object[] args = new Object[entryComponents.length];
            boolean changed = false;

            for (int index = 0; index < entryComponents.length; index++) {
                RecordComponent component = entryComponents[index];
                Object value = component.getAccessor().invoke(entry);

                if (component.getType() == int.class && component.getName().equals("latency")) {
                    changed = !Objects.equals(value, fakeLatency);
                    value = fakeLatency;
                }

                args[index] = value;
            }

            if (!changed) {
                return entry;
            }

            return entryConstructor.newInstance(args);
        }
    }
}
