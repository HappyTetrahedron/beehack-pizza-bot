const puppeteer = require('puppeteer');

const orders = [
    { articleId: 1, count: 1 },
    { articleId: 492, count: 1 }
];

const PERSONAL_DATA = {
    firstName: 'Beekeeper',
    lastName: 'Beekeeper',
    email: 'kacper.grabowski@beekeeper.io',
    phone: '+41 78 123 45 67',
    company: 'Beekeeper',
    street: 'Hönggerstrasse',
    postalCode: '8037',
    city: 'Zürich',
    buildingNumber: '65',
};


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
    const browser = await puppeteer.launch({ headless: false, args: ['--start-fullscreen'] });
    const page = await browser.newPage();
    await page.setViewport({ width: 2000, height: 1500});
    await page.goto('https://www.dieci.ch/en/index');
    await page.type('#plzEntry', '8037');
    await page.click('#orderPizza')
    await page.waitForSelector('.article-container')

    await orders.reduce((promiseChain, order) => {
        return promiseChain.then(() => orderPizza(page, order));
    }, Promise.resolve());
 await page.waitForSelector(".Reached_minOrderPrice", { visible: true, timeout: 5000 });
    await page.click(".Reached_minOrderPrice");
    await page.waitForSelector(".goto-shopping-cart .btn", { visible: true, timeout: 5000 });
    await page.click(".goto-shopping-cart .btn");
    await page.waitForSelector('[name="order_comment"]', { visible: true, timeout: 5000 });
    // await page.click('label[for="radio_gender_m]');
    await page.type('#firstname', PERSONAL_DATA.firstName);
    await page.type('#lastname', PERSONAL_DATA.lastName);
    await page.type('#communication_email', PERSONAL_DATA.email);
    await page.type('#communication_phonenumber', PERSONAL_DATA.phone);
    // await page.type('input[name="customerdata[customer_companyname]"]', PERSONAL_DATA.company);
    await page.type('#input_zip', PERSONAL_DATA.postalCode);
    await page.type('#input_city', PERSONAL_DATA.city);
    await page.type('#input_street', PERSONAL_DATA.street);


})();