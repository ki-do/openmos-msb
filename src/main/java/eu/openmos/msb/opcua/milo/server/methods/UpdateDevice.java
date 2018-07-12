package eu.openmos.msb.opcua.milo.server.methods;

import eu.openmos.agentcloud.config.ConfigurationLoader;
import eu.openmos.model.Module;
import eu.openmos.model.Recipe;
import eu.openmos.msb.database.interaction.DatabaseInteraction;
import eu.openmos.msb.datastructures.DACManager;
import eu.openmos.msb.datastructures.DeviceAdapter;
import eu.openmos.msb.datastructures.DeviceAdapterOPC;
import static eu.openmos.msb.datastructures.MSBConstants.PROJECT_PATH;
import static eu.openmos.msb.datastructures.MSBConstants.XML_PATH;
import eu.openmos.msb.opcua.milo.client.MSBClientSubscription;
import static eu.openmos.msb.opcua.milo.server.OPCServersDiscoverySnippet.browseInstaceHierarchyNode;
import eu.openmos.msb.starter.MSB_gui;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fabio.miranda
 */
public class UpdateDevice
{

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @UaMethod
  public void invoke(
          AnnotationBasedInvocationHandler.InvocationContext context,
          @UaInputArgument(
                  name = "da_id",
                  description = "Device Adapter to re-browse") String da_id,
          @UaOutputArgument(
                  name = "result",
                  description = "The result") AnnotationBasedInvocationHandler.Out<Integer> result)
  {
    try
    {
      logger.debug("Update Device invoked! '{}'", context.getObjectNode().getBrowseName().getName());
      //rebrowse namespace of targert DA
      String da_name = DatabaseInteraction.getInstance().getDeviceAdapterNameByAmlID(da_id);
      
      DeviceAdapter da;
      
      if (da_name.equals(""))
        da = DACManager.getInstance().getDeviceAdapterFromModuleID(da_id);  
      else
        da = DACManager.getInstance().getDeviceAdapterbyName(da_name);

      DeviceAdapter auxDA = rebrowseNamespace(da);

      if (auxDA != null)
      {
        da = auxDA;
        validateModules_in_DB(da);
        validateRecipes_in_DB(da);
        
        //update tables
        MSB_gui.fillModulesTable();
        MSB_gui.fillRecipesTable();
        
      } else
      {
        System.out.println("ERROR rebrownsing DA: " + da.getSubSystem().getName());
        //WHAT TO DO?
        //DA have corrupted data
      }
      logger.debug("Update Device FINISHED!");
      result.set(1);
    } catch (Exception ex)
    {
      logger.debug("Update Device ERROR! '{}'", ex.getMessage());
      result.set(0);
    }
  }

  private DeviceAdapter rebrowseNamespace(DeviceAdapter da)
  {
    DeviceAdapterOPC da_OPC = (DeviceAdapterOPC) da;
    MSBClientSubscription msbClient = da_OPC.getClient();
    OpcUaClient client = msbClient.getClientObject();

    System.out.println("\n");
    System.out.println("***** Starting namespace re-browsing ***** \n");

    try
    {
      Element node = new Element("DeviceAdapter");
      Set<String> ignore = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      ignore.addAll(Arrays.asList(ConfigurationLoader.getMandatoryProperty("openmos.msb.opcua.parser.ignore").split(",")));

      NodeId InstaceHierarchyNode = browseInstaceHierarchyNode("", client, new NodeId(0, 84));
      if (InstaceHierarchyNode != null)
      {
        System.out.println("Browse instance Hierarchy ended with: " + InstaceHierarchyNode.getIdentifier().toString());
        node.addContent(msbClient.browseNode(client,
                InstaceHierarchyNode,
                Integer.valueOf(ConfigurationLoader.getMandatoryProperty("openmos.msb.opcua.parser.level")),
                ignore));
      }

      Element nSkills = new Element("Skills");
      nSkills.addContent(msbClient.browseNode(client,
              new NodeId(2, ConfigurationLoader.getMandatoryProperty("openmos.msb.opcua.parser.namespace.skills")),
              Integer.valueOf(ConfigurationLoader.getMandatoryProperty("openmos.msb.opcua.parser.level.skills")),
              ignore));

      // print to file the XML structure extracted from the browsing process             
      XMLOutputter xmlOutput = new XMLOutputter();
      xmlOutput.setFormat(Format.getPrettyFormat());

      //xmlOutput.output(node, new FileWriter(XML_PATH + "\\main_" + da.getSubSystem().getName() + ".xml", false));
      //xmlOutput.output(nSkills, new FileWriter(XML_PATH + "\\skills_" + da.getSubSystem().getName() + ".xml", false));

      System.out.println("Starting DA Parser **********************");

      boolean ok = da.parseDNToObjects(client, node, nSkills, false);

      if (ok)
      {
        return da;
      } else
      {
        return null;
      }

    } catch (Exception ex)
    {

    }
    System.out.println("***** End namespace browsing ***** \n\n");
    return null;
  }

  private void validateRecipes_in_DB(DeviceAdapter da)
  {
    //RECIPES
    List<String> auxRecipesDB = DatabaseInteraction.getInstance().getRecipesIDByDAName(da.getSubSystem().getName());
    List<String> idsFound = new ArrayList<>();
    
    List<Recipe> tempRepList = da.getSubSystem().getRecipes();
    for (Module auxMod : da.getListOfModules())
    {
      tempRepList.addAll(auxMod.getRecipes());
    }

    for (String recipeID_DB : auxRecipesDB)
    {      
      boolean notFound = true;
      for (Recipe recipe : tempRepList)
      {
        if (recipe.getUniqueId().equals(recipeID_DB))
        {
          notFound = false;
          idsFound.add(recipe.getUniqueId());
          //update recipe fields?
          break;
        }
      }
      if (notFound)
      {
        Boolean a = DatabaseInteraction.getInstance().remove_recipe_from_SR(recipeID_DB);
        int aux = DatabaseInteraction.getInstance().removeRecipeById(recipeID_DB);
        logger.info("" + aux);
      }
    }

    for (Recipe recipe : tempRepList)
    {
      if (!idsFound.contains(recipe.getUniqueId()))
      {
        DACManager.getInstance().registerRecipe(da.getSubSystem().getName(), recipe.getUniqueId(), recipe.getSkill().getName(),
                "true", recipe.getName(), recipe.getInvokeObjectID(), recipe.getInvokeMethodID());
      }
    }

  }
  
  private void validateModules_in_DB(DeviceAdapter da)
  {
    //RECIPES
    String da_id_db = DatabaseInteraction.getInstance().getDA_DB_IDbyAML_ID(da.getSubSystem().getUniqueId());
    List<String> auxModulesAML_DB_ID = DatabaseInteraction.getInstance().getModulesAML_ID_ByDA_DB_ID(da_id_db);

    List<String> idsFound = new ArrayList<>();
    for (String moduleAML_DB_ID : auxModulesAML_DB_ID)
    {
      boolean notFound = true;
      
      for (Module module : da.getSubSystem().getModules())
      {
        if (module.getUniqueId().equals(moduleAML_DB_ID))
        {
          notFound = false;
          idsFound.add(module.getUniqueId());
          break;
        }
      }
      if (notFound)
      {
        DatabaseInteraction.getInstance().removeModuleByID(moduleAML_DB_ID);
      }
    }

    for (Module module : da.getSubSystem().getModules())
    {
      if (!idsFound.contains(module.getUniqueId()))
      {
        DACManager.getInstance().registerModule(da.getSubSystem().getName(), module.getName(), 
                module.getUniqueId(), module.getStatus(), module.getAddress());
      }
    }

  }
   
}
