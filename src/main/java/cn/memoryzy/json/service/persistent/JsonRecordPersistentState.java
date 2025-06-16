// package cn.memoryzy.json.service.persistent;
//
// import cn.memoryzy.json.service.persistent.state.v2.JsonRecordState;
// import com.intellij.openapi.components.PersistentStateComponent;
// import com.intellij.openapi.components.RoamingType;
// import com.intellij.openapi.components.State;
// import com.intellij.openapi.components.Storage;
// import com.intellij.openapi.project.Project;
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;
//
// /**
//  * @author Memory
//  * @since 2025/6/15
//  */
// @State(name = "JsonAssistantHistory", storages = {@Storage(value = "JsonAssistantHistoryStateV2.xml", roamingType = RoamingType.DISABLED)})
// public class JsonRecordPersistentState implements PersistentStateComponent<JsonRecordState>  {
//
//     public static JsonRecordPersistentState getInstance(Project project) {
//         return project.getService(JsonRecordPersistentState.class);
//     }
//
//     public JsonRecordState recordState = new JsonRecordState();
//
//     @Override
//     public @Nullable JsonRecordState getState() {
//         return recordState;
//     }
//
//     @Override
//     public void loadState(@NotNull JsonRecordState state) {
//         this.recordState = state;
//     }
// }
