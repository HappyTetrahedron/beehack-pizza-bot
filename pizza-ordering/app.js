const puppeteer = require('puppeteer');

const orders = [
    { articleId: 1, count: 1 },
    { articleId: 492, count: 1 }
];

async function orderPizza(page, { articleId, count }) {
    console.log('Ordering pizza', articleId);

    const sizeDropdownOptionSelector = `.dropdown-menu .choose-article[data-article-id="${articleId}"]`;
    const sizeDropdownOption = await page.$(sizeDropdownOptionSelector);
    const productId = await page.evaluate(
        sizeDropdown => sizeDropdown.closest("[data-article-number]").getAttribute("data-article-number")
    , sizeDropdownOption);
    const productElement = await page.$(`[data-article-number="${productId}"]`);
    // console.log(productElement);
    const dropdown = await productElement.$(".dropdown-toggle");
    const addToCartButton = await productElement.$(".article-price-and-choose-btn :not(.choose-toppings) .choose-article");
    console.log('Progress', articleId);
    await dropdown.click()
    await page.waitForSelector(sizeDropdownOptionSelector, { visible: true, timeout: 2000 });
    await sizeDropdownOption.click();
    await page.screenshot({path: 'b.png'});
    await addToCartButton.click();
    await page.screenshot({path: `${articleId}.png`});
    console.log('Done', articleId);
}

(async () => {
    const browser = await puppeteer.launch({ headless: false });
    const page = await browser.newPage();
    await page.setViewport({ width: 2000, height: 1500});
    await page.goto('https://www.dieci.ch/en/index');
    await page.type('#plzEntry', '8037');
    await page.click('#orderPizza')
    await page.waitForSelector('.article-container')
    await page.screenshot({path: 'a.png'});

    await orders.reduce((promiseChain, order) => {
        return promiseChain.then(() => orderPizza(page, order));
    }, Promise.resolve());
    await page.waitForSelector(".Reached_minOrderPrice", { visible: true, timeout: 5000 });
    await page.click(".Reached_minOrderPrice");
    await page.waitForSelector(".goto-shopping-cart .btn", { visible: true, timeout: 5000 });
    await page.click(".goto-shopping-cart .btn");
    await page.waitForSelector('[name="order_comment"]', { visible: true, timeout: 5000 });
    await page.screenshot({path: 'b.png'});
})();